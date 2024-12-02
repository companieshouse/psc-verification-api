package uk.gov.companieshouse.pscverificationapi.controller.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.pscverificationapi.controller.impl.BaseControllerIT.SECOND_INSTANT;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
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
import org.springframework.validation.FieldError;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.api.model.common.ResourceLinks;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.patch.model.PatchResult;
import uk.gov.companieshouse.pscverificationapi.controller.PscVerificationController;
import uk.gov.companieshouse.pscverificationapi.error.RetrievalFailureReason;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.exception.InvalidPatchException;
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

    private static final LocalDate TEST_DATE = LocalDate.of(2024, 5, 5);

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
    private Map<String, Object> mergePatch;

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
        final var links = expectEntitySavedWithLinks();
        pscVerificationApi = PscVerificationApi.newBuilder()
                .createdAt(FIRST_INSTANT)
                .updatedAt(FIRST_INSTANT)
                .data(filing).links(links)
                .build();
        entityWithLinks = PscVerification.newBuilder(entity).links(links).build();
        var mergeVerificationDetails = new HashMap<>(Map.of());
        mergePatch = new HashMap<>();
        mergePatch.put("verification_details", mergeVerificationDetails);
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
        final var resourceIdUri = REQUEST_URI.relativize(Objects.requireNonNull(location));

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
    void updatePscVerification() {
        final var success = new PatchResult();
        final var updatedEntity = PscVerification.newBuilder(entityWithLinks)
            .updatedAt(SECOND_INSTANT)
            .build();

        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.of(entityWithLinks))
            .thenReturn(Optional.of(updatedEntity));
        when(
            pscVerificationService.requestMatchesResourceSelf(request, entityWithLinks)).thenReturn(
            true);
        when(pscVerificationService.patch(eq(FILING_ID), anyMap())).thenReturn(success);

        final var response = testController.updatePscVerification(TRANS_ID, FILING_ID,
            mergePatch, request);
        final var expectedBody = filingMapper.toApi(updatedEntity);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody(), is(expectedBody));
        assertThat(response.getBody().getUpdatedAt(),
            is(not(equalTo(response.getBody().getCreatedAt()))));
        assertThat(response.getHeaders().getLocation(), is(entityWithLinks.getLinks().self()));

    }

    @Test
    void updatePscVerificationNoUvid() {

        final var success = new PatchResult();
        final var updatedEntity = PscVerification.newBuilder(entityWithLinks)
            .updatedAt(SECOND_INSTANT)
            .build();

        VerificationDetails verificationNoUvid = VerificationDetails.newBuilder()
            .statements(EnumSet.of(VerificationStatementConstants.INDIVIDUAL_VERIFIED))
            .build();
        entityWithLinks = PscVerification.newBuilder(entityWithLinks)
            .data(PscVerificationData.newBuilder(filing).verificationDetails(verificationNoUvid).build())
            .updatedAt(SECOND_INSTANT)
            .build();

        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.of(entityWithLinks))
            .thenReturn(Optional.of(updatedEntity));

        when(
            pscVerificationService.requestMatchesResourceSelf(request, entityWithLinks)).thenReturn(
            true);
        when(pscVerificationService.patch(eq(FILING_ID), anyMap())).thenReturn(success);

        final var response = testController.updatePscVerification(TRANS_ID, FILING_ID,
            mergePatch, request);
        final var expectedBody = filingMapper.toApi(updatedEntity);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody(), is(expectedBody));
        assertThat(response.getBody().getUpdatedAt(),
            is(not(equalTo(response.getBody().getCreatedAt()))));
        assertThat(response.getHeaders().getLocation(), is(entityWithLinks.getLinks().self()));

    }

    @Test
    void updatePscVerificationPatchUvidMatch() {

        final var success = new PatchResult();

        var mergeVerificationDetails = new HashMap<>(Map.of());
        mergeVerificationDetails.put("uvid", UVID);
        mergePatch.clear();
        mergePatch.put("verification_details", mergeVerificationDetails);


        final var updatedEntity = PscVerification.newBuilder(entityWithLinks)
            .updatedAt(SECOND_INSTANT)
            .build();

        entityWithLinks = PscVerification.newBuilder(entityWithLinks)
            .updatedAt(SECOND_INSTANT)
            .build();

        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.of(entityWithLinks))
            .thenReturn(Optional.of(updatedEntity));

        when(
            pscVerificationService.requestMatchesResourceSelf(request, entityWithLinks)).thenReturn(
            true);
        when(pscVerificationService.patch(eq(FILING_ID), anyMap())).thenReturn(success);

        final var response = testController.updatePscVerification(TRANS_ID, FILING_ID,
            mergePatch, request);
        final var expectedBody = filingMapper.toApi(updatedEntity);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody(), is(expectedBody));
        assertThat(response.getBody().getUpdatedAt(),
            is(not(equalTo(response.getBody().getCreatedAt()))));
        assertThat(response.getHeaders().getLocation(), is(entityWithLinks.getLinks().self()));

    }

    @Test
    void updatePscVerificationNoNameMismatchPatch() {

        final var success = new PatchResult();

        var mergeVerificationDetails = new HashMap<>(Map.of());
        mergeVerificationDetails.put("uvid", "12345678");
        mergePatch.clear();
        mergePatch.put("verification_details", mergeVerificationDetails);

        final var updatedEntity = PscVerification.newBuilder(entityWithLinks)
            .updatedAt(SECOND_INSTANT)
            .build();

        entityWithLinks = PscVerification.newBuilder(entityWithLinks)
            .updatedAt(SECOND_INSTANT)
            .build();

        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.of(entityWithLinks))
            .thenReturn(Optional.of(updatedEntity));

        when(
            pscVerificationService.requestMatchesResourceSelf(request, entityWithLinks)).thenReturn(
            true);
        when(pscVerificationService.patch(eq(FILING_ID), anyMap())).thenReturn(success);

        final var response = testController.updatePscVerification(TRANS_ID, FILING_ID,
            mergePatch, request);
        final var expectedBody = filingMapper.toApi(updatedEntity);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody(), is(expectedBody));
        assertThat(response.getBody().getUpdatedAt(),
            is(not(equalTo(response.getBody().getCreatedAt()))));
        assertThat(response.getHeaders().getLocation(), is(entityWithLinks.getLinks().self()));

    }

    @Test
    void updatePscVerificationNameMismatchPatchPresent() {

        final var success = new PatchResult();

        var mergeVerificationDetails = new HashMap<>(Map.of());
        mergeVerificationDetails.put("uvid", "12345678");
        mergeVerificationDetails.put("name_mismatch_reason", "PREFER_NOT_TO_SAY");
        mergePatch.clear();
        mergePatch.put("verification_details", mergeVerificationDetails);

        final var updatedEntity = PscVerification.newBuilder(entityWithLinks)
            .updatedAt(SECOND_INSTANT)
            .build();

        entityWithLinks = PscVerification.newBuilder(entityWithLinks)
            .updatedAt(SECOND_INSTANT)
            .build();

        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.of(entityWithLinks))
            .thenReturn(Optional.of(updatedEntity));

        when(
            pscVerificationService.requestMatchesResourceSelf(request, entityWithLinks)).thenReturn(
            true);
        when(pscVerificationService.patch(eq(FILING_ID), anyMap())).thenReturn(success);

        final var response = testController.updatePscVerification(TRANS_ID, FILING_ID,
            mergePatch, request);
        final var expectedBody = filingMapper.toApi(updatedEntity);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getBody(), is(expectedBody));
        assertThat(response.getBody().getUpdatedAt(),
            is(not(equalTo(response.getBody().getCreatedAt()))));
        assertThat(response.getHeaders().getLocation(), is(entityWithLinks.getLinks().self()));

    }

    @Test
    void updatePscVerificationWhenPatchProviderRetrievalFails() {
        final var failure = new PatchResult(RetrievalFailureReason.FILING_NOT_FOUND);

        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.of(entityWithLinks));
        when(
            pscVerificationService.requestMatchesResourceSelf(request, entityWithLinks)).thenReturn(
            true);
        when(pscVerificationService.patch(eq(FILING_ID), anyMap())).thenReturn(failure);

        final var exception = assertThrows(FilingResourceNotFoundException.class,
            () -> testController.updatePscVerification(TRANS_ID, FILING_ID, mergePatch, request));

        assertThat(exception.getMessage(), is(FILING_ID));
    }

    @Test
    void updatePscVerificationWhenValidationFails() {
        final var error = new FieldError("patched", "dateOfBirth", TEST_DATE, false,
            new String[]{"future.date.patched.dateOfBirth", "future.date.dateOfBirth",
                "future" + ".date.java.time.LocalDate", "future.date"},
            new Object[]{TEST_DATE}, "bad date");
        final var failure = new PatchResult(List.of(error));

        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.of(entityWithLinks));
        when(
            pscVerificationService.requestMatchesResourceSelf(request, entityWithLinks)).thenReturn(
            true);
        when(pscVerificationService.patch(eq(FILING_ID), anyMap())).thenReturn(failure);

        final var exception = assertThrows(InvalidPatchException.class,
            () -> testController.updatePscVerification(TRANS_ID, FILING_ID, mergePatch, request));

        assertThat(exception.getFieldErrors(), contains(error));
    }

    @Test
    void updatePscVerificationWhenSelfLinkMatchFails() {

        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.of(entityWithLinks));
        when(
            pscVerificationService.requestMatchesResourceSelf(request, entityWithLinks)).thenReturn(
            false);

        final var exception = assertThrows(FilingResourceNotFoundException.class,
            () -> testController.updatePscVerification(TRANS_ID, FILING_ID, mergePatch, request));

        assertThat(exception.getMessage(), is(FILING_ID));

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
        final var clock1 = Clock.fixed(FIRST_INSTANT, ZoneId.of("UTC"));
        final var clock2 = Clock.fixed(SECOND_INSTANT, ZoneId.of("UTC"));
        when(clock.instant()).thenReturn(clock1.instant(), clock2.instant());

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
