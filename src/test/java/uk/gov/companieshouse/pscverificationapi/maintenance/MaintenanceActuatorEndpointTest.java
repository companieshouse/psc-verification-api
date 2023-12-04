package uk.gov.companieshouse.pscverificationapi.maintenance;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.model.common.MaintenanceCheck;
import uk.gov.companieshouse.api.model.common.ServiceStatus;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class MaintenanceActuatorEndpointTest {
    private static final Instant FIXED_NOW = Instant.parse("2023-12-25T01:23:45Z");

    private MaintenanceActuatorEndpoint testEndpoint;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        testEndpoint = new MaintenanceActuatorEndpoint(Clock.fixed(FIXED_NOW, ZoneId.of("UTC")),
            logger);
    }

    public static Stream<Arguments> blankConfigs() {
        return Stream.of(arguments("", null, null), arguments("3 Dec 23 00:30 GMT", null, null),
            arguments("", "3 Dec 23 00:30 GMT", null));
    }

    @ParameterizedTest
    @MethodSource("blankConfigs")
    void resultWhenTimesAreBlank(final String start, final String end, final String message) {
        setupPeriodConfigValues(start, end, message);

        final MaintenanceCheck resultNow = testEndpoint.check();

        assertThat(resultNow.status(), is(ServiceStatus.UP));
        assertThat(resultNow.message(), is("No planned maintenance is configured"));

    }

    @Test
    void checkWhenPeriodSetForFuture() {
        setupPeriodConfigValues("3 Jan 24 00:30 GMT", "3 Jan 24 02:30 GMT", null);

        final MaintenanceCheck resultNow = testEndpoint.check();

        assertThat(resultNow.status(), is(ServiceStatus.UP));
        assertThat(resultNow.message(), is("Planned maintenance has been configured"));
        assertThat(resultNow.maintenanceStart(), is("2024-01-03T00:30:00Z"));
        assertThat(resultNow.maintenanceEnd(), is("2024-01-03T02:30:00Z"));

    }

    @Test
    void checkWhenPeriodSetForPast() {
        setupPeriodConfigValues("3 Dec 23 00:30 GMT", "3 Dec 23 02:30 GMT", null);

        final MaintenanceCheck resultNow = testEndpoint.check();

        assertThat(resultNow.status(), is(ServiceStatus.UP));
        assertThat(resultNow.message(), is("Planned maintenance has been configured"));
        assertThat(resultNow.maintenanceStart(), is("2023-12-03T00:30:00Z"));
        assertThat(resultNow.maintenanceEnd(), is("2023-12-03T02:30:00Z"));

    }

    @Test
    void checkWhenPeriodSetAndOngoing() {
        setupPeriodConfigValues("25 Dec 23 00:30 GMT", "25 Dec 23 02:30 GMT",
            "UNAVAILABLE - PLANNED MAINTENANCE");

        final MaintenanceCheck resultNow = testEndpoint.check();

        assertThat(resultNow.status(), is(ServiceStatus.OUT_OF_SERVICE));
        assertThat(resultNow.message(), is("UNAVAILABLE - PLANNED MAINTENANCE"));
        assertThat(resultNow.maintenanceStart(), is("2023-12-25T00:30:00Z"));
        assertThat(resultNow.maintenanceEnd(), is("2023-12-25T02:30:00Z"));

    }

    @Test
    void checkWhenStartTimeInvalidNoTimezone() {
        setupPeriodConfigValues("25 Dec 23 00:30", "25 Dec 23 02:30 GMT",
            "UNAVAILABLE - PLANNED MAINTENANCE");

        final MaintenanceCheck resultNow = testEndpoint.check();

        assertThat(resultNow.status(), is(ServiceStatus.UP));
        assertThat(resultNow.message(),
            is("Error parsing configuration: PLANNED_MAINTENANCE_START_TIME: Text '25 Dec 23 " +
                "00:30' could not be parsed: Unable to obtain ZonedDateTime from " +
                "TemporalAccessor: {},ISO resolved to 2023-12-25T00:30 of type java.time.format"
                + ".Parsed"));
        assertThat(resultNow.maintenanceStart(), is("25 Dec 23 00:30"));
        assertThat(resultNow.maintenanceEnd(), is("25 Dec 23 02:30 GMT"));
    }

    @Test
    void checkWhenEndTimeInvalidNoSpaceAfterTime() {
        setupPeriodConfigValues("25 Dec 23 00:30 GMT", "5 Jan 24 02:30+01",
            "UNAVAILABLE - PLANNED MAINTENANCE");

        final MaintenanceCheck resultNow = testEndpoint.check();

        assertThat(resultNow.status(), is(ServiceStatus.UP));
        assertThat(resultNow.message(),
            is("Error parsing configuration: PLANNED_MAINTENANCE_END_TIME: Text '5 Jan 24 " + "02"
                + ":30+01' could not be parsed, unparsed text found at index 14"));
        assertThat(resultNow.maintenanceStart(), is("2023-12-25T00:30:00Z"));
        assertThat(resultNow.maintenanceEnd(), is("5 Jan 24 02:30+01"));
    }

    @Test
    void checkWhenEndTimeInvalidSingleDigitOffset() {
        setupPeriodConfigValues("", "5 Jan 24 02:30+01",
            "UNAVAILABLE - PLANNED MAINTENANCE");

        final MaintenanceCheck resultNow = testEndpoint.check();

        assertThat(resultNow.status(), is(ServiceStatus.UP));
        assertThat(resultNow.message(),
            is("Error parsing configuration: PLANNED_MAINTENANCE_END_TIME: Text '5 Jan 24 " + "02"
                + ":30+01' could not be parsed, unparsed text found at index 14"));
        assertThat(resultNow.maintenanceStart(), is(nullValue()));
        assertThat(resultNow.maintenanceEnd(), is("5 Jan 24 02:30+01"));
    }

    private void setupPeriodConfigValues(final String start, final String end,
        final String message) {
        ReflectionTestUtils.setField(testEndpoint, "outOfServiceStart", start);
        ReflectionTestUtils.setField(testEndpoint, "outOfServiceEnd", end);
        ReflectionTestUtils.setField(testEndpoint, "outOfServiceMessage", message);
    }

}