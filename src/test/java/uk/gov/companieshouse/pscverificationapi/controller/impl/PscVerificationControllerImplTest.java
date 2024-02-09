package uk.gov.companieshouse.pscverificationapi.controller.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.BindingResult;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.api.model.common.ResourceLinks;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.controller.PscVerificationController;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.model.mapper.PscVerificationMapper;
import uk.gov.companieshouse.pscverificationapi.model.mapper.PscVerificationMapperImpl;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;
import uk.gov.companieshouse.pscverificationapi.service.TransactionService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@ExtendWith(SpringExtension.class) // JUnit 5
@ContextConfiguration(classes = {PscVerificationMapperImpl.class})
class PscVerificationControllerImplTest {
    public static final String TRANS_ID = "117524-754816-491724";
    private static final String PSC_ID = "1kdaTltWeaP1EB70SSD9SLmiK5Y";
    private static final String COMPANY_NUMBER = "00006400";
    private static final String UVID = "0xDEADBEEF";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    public static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final Instant FIRST_INSTANT = Instant.parse("2024-10-15T09:44:08.108Z");
    private static final URI REQUEST_URI = URI.create(
        "/transactions/" + TRANS_ID + "/persons-with-significant-control-verification");

    private static final URI SELF_URI = URI.create(REQUEST_URI + FILING_ID);
    private static final URI VALIDATION_URI = URI.create(
            SELF_URI + "/validation_status");

    private PscVerificationController testController;

    @Mock
    private TransactionService transactionService;
    @Mock
    private PscVerificationService pscVerificationService;
    @Autowired
    private PscVerificationMapper filingMapper;
    @Mock
    private Clock clock;
    @Mock
    private Logger logger;
    @Mock
    private BindingResult result;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Transaction transaction;
    private VerificationDetails verification;
    private PscVerificationApi pscVerificationApi;
    private PscVerificationData filing;
    private PscVerification entity;
    private PscVerification entityWithLinks;

    public static Stream<Arguments> provideCreateParams() {
        return Stream.of(Arguments.of(false, false, false), Arguments.of(true, true, true));
    }

    @BeforeEach
    void setUp() {
        testController = new PscVerificationControllerImpl(transactionService,
            pscVerificationService, filingMapper, clock, logger);
        verification = VerificationDetails.newBuilder()
            .uvid(UVID)
            .statements(EnumSet.of(VerificationStatementConstants.INDIVIDUAL_VERIFIED))
            .build();
        filing = PscVerificationData.newBuilder()
            .verificationDetails(verification)
            .companyNumber(COMPANY_NUMBER)
            .pscAppointmentId(PSC_ID)
            .build();
        entity = PscVerification.newBuilder()
            .createdAt(FIRST_INSTANT)
            .updatedAt(FIRST_INSTANT)
            .data(filing)
            .build();
        pscVerificationApi = PscVerificationApi.newBuilder()
                .createdAt(FIRST_INSTANT)
                .updatedAt(FIRST_INSTANT)
                .data(filing)
                .links(expectEntitySavedWithLinks())
                .build();
        entityWithLinks = PscVerification.newBuilder(entity)
                .links(expectEntitySavedWithLinks()).build();
    }

    @ParameterizedTest(
        name = "[{index}] null binding result={0}, null passthrough={1}, null transaction={2}")
    @MethodSource("provideCreateParams")
    void createPscVerification(final boolean nullBindingResult, final boolean nullPassthrough,
        final boolean nullTransaction) {
        expectTransactionIsPresent(nullPassthrough, nullTransaction);
        final var links = expectEntitySavedWithLinks();

        final var response = testController.createPscVerification(TRANS_ID,
            nullTransaction ? null : transaction, filing, nullBindingResult ? null : result,
            request);

        final var location = response.getHeaders().getLocation();
        final var resourceIdUri = REQUEST_URI.relativize(location);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(location, is(notNullValue()));
        assertThat(resourceIdUri.toString(), is(FILING_ID));

        final var expectedApi = PscVerificationApi.newBuilder()
            .createdAt(FIRST_INSTANT)
            .updatedAt(FIRST_INSTANT)
            .data(filing)
            .links(links)
            .build();

        assertThat(response.getBody(), is(equalTo(expectedApi)));
    }

    @Test
    void getPscVerificationWhenFound() {

        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.of(entityWithLinks));
        when(pscVerificationService.requestMatchesResourceSelf(request, entityWithLinks)).thenReturn(true);

        final var response =
                testController.getPscVerification(TRANS_ID, FILING_ID, request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(equalTo(pscVerificationApi)));
    }

    @Test
    void getFilingForReviewWhenNotFound() {
        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.empty());

        final var response =
                testController.getPscVerification(TRANS_ID, FILING_ID, request);

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    private ResourceLinks expectEntitySavedWithLinks() {
        when(request.getRequestURI()).thenReturn(REQUEST_URI.toString());
        when(clock.instant()).thenReturn(Clock.fixed(FIRST_INSTANT, ZoneId.of("UTC")).instant());

        final var timestampedEntity = PscVerification.newBuilder(entity)
            .createdAt(FIRST_INSTANT)
            .updatedAt(FIRST_INSTANT)
            .build();
        final var savedEntity = PscVerification.newBuilder(timestampedEntity).id(FILING_ID).build();
        final var self = URI.create(REQUEST_URI.toString() + "/" + FILING_ID);

        when(pscVerificationService.save(timestampedEntity)).thenReturn(savedEntity);

        final var links = ResourceLinks.newBuilder()
            .self(self)
            .validationStatus(
                UriComponentsBuilder.fromUri(self).pathSegment("validation_status").build().toUri())
            .build();
        final var resavedEntity = PscVerification.newBuilder(savedEntity).links(links).build();

        when(pscVerificationService.save(resavedEntity)).thenReturn(resavedEntity);
        return links;
    }

    private void expectTransactionIsPresent(boolean nullPassthrough, boolean nullTransaction) {
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(
            nullPassthrough ? null : PASSTHROUGH_HEADER);
        if (nullTransaction) {
            when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(
                transaction);
        }
    }
}
