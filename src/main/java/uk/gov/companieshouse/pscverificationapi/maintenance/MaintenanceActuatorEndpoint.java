package uk.gov.companieshouse.pscverificationapi.maintenance;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.common.MaintenanceCheck;
import uk.gov.companieshouse.api.model.common.ServiceStatus;
import uk.gov.companieshouse.logging.Logger;

@Component
@Endpoint(id = "maintenance")
public class MaintenanceActuatorEndpoint {

    private static final String NO_PLANNED_MAINTENANCE_CONFIGURED =
        "No planned maintenance is configured";

    private final Clock clock;
    private final Logger logger;

    @Value("${out-of-service.period.start}")
    private String outOfServiceStart;

    @Value("${out-of-service.period.end}")
    private String outOfServiceEnd;

    @Value("${out-of-service.period.message}")
    private String outOfServiceMessage;

    @Autowired
    public MaintenanceActuatorEndpoint(final Clock clock, final Logger logger) {
        this.clock = clock;
        this.logger = logger;
    }

    /**
     * Check the current time against any planned maintenance period scheduled through
     * configuration.
     *
     * @return {@link MaintenanceCheck} containing details of the result
     */
    @ReadOperation(produces = "application/json")
    @Bean
    public MaintenanceCheck check() {
        ZonedDateTime startDateTime = null;
        ZonedDateTime endDateTime = null;

        try {
            startDateTime = StringUtils.isBlank(outOfServiceStart) ? null : parseZonedDateTime(
                outOfServiceStart);
        } catch (DateTimeParseException e) {
            final String errorMessage = "Error parsing configuration: " +
                "PLANNED_MAINTENANCE_START_TIME: " + e.getMessage();

            logger.error(errorMessage);
            return new MaintenanceCheck(ServiceStatus.UP, errorMessage, outOfServiceStart,
                outOfServiceEnd);
        }
        try {
            endDateTime = StringUtils.isBlank(outOfServiceEnd) ? null : parseZonedDateTime(
                outOfServiceEnd);
        } catch (DateTimeParseException e) {
            final String errorMessage = "Error parsing configuration: " +
                "PLANNED_MAINTENANCE_END_TIME: " + e.getMessage();

            logger.error(errorMessage);
            return new MaintenanceCheck(ServiceStatus.UP, errorMessage,
                startDateTime == null ? null : startDateTime.format(DateTimeFormatter.ISO_INSTANT),
                outOfServiceEnd);
        }

        final ZonedDateTime now = ZonedDateTime.now(clock);

        if (startDateTime != null && endDateTime != null) {
            // non-inclusive range: startDateTime < now < endDateTime
            if (now.isAfter(startDateTime) && now.isBefore(endDateTime)) {
                logger.info("Planned maintenance is ongoing - ending at " + endDateTime.format(
                    DateTimeFormatter.ISO_INSTANT));

                return new MaintenanceCheck(ServiceStatus.OUT_OF_SERVICE, outOfServiceMessage,
                    startDateTime.format(DateTimeFormatter.ISO_INSTANT),
                    endDateTime.format(DateTimeFormatter.ISO_INSTANT));
            }
            else {
                logger.info("No planned maintenance is ongoing");
                return new MaintenanceCheck(ServiceStatus.UP,
                    "Planned maintenance has been configured",
                    startDateTime.format(DateTimeFormatter.ISO_INSTANT),
                    endDateTime.format(DateTimeFormatter.ISO_INSTANT));
            }
        }
        else {
            logger.info(NO_PLANNED_MAINTENANCE_CONFIGURED);
            return new MaintenanceCheck(ServiceStatus.UP, NO_PLANNED_MAINTENANCE_CONFIGURED);
        }
    }

    private static ZonedDateTime parseZonedDateTime(final String zoned) {
        return ZonedDateTime.parse(zoned,
            DateTimeFormatter.ofPattern("d MMM yy HH:mm[ z][ x]", Locale.ENGLISH));
    }
}
