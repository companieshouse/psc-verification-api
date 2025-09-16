package uk.gov.companieshouse.pscverificationapi.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.pscverificationapi.enumerations.PscType.INDIVIDUAL;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.delta.pscfullrecord.request.PscFullRecordGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.psc.IndividualFullRecord;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.exception.PscLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.sdk.companieshouse.InternalApiClientService;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

@ExtendWith(MockitoExtension.class)
class PscLookupServiceImplTest extends TestBaseService {

    private static final String PSC_ID = "654321";
    private static final String UVID_CODE = "XY222222223";
    private static final VerificationDetails VERIFICATION_DETAILS =
        VerificationDetails.newBuilder().uvid(UVID_CODE)
            .build();
    private static final PscVerificationData PSC_VERIFICATION_DATA =
        PscVerificationData.newBuilder()
            .pscNotificationId(PSC_ID)
            .companyNumber(COMPANY_NUMBER)
            .verificationDetails(VERIFICATION_DETAILS)
            .build();
    public static final String FULL_RECORD = "/full_record";
    public static final String PERSONS_WITH_SIGNIFICANT_CONTROL = "/persons-with-significant-control/";
    public static final String COMPANY = "/company/";

    @Mock
    private InternalApiClientService apiClientService;
    @Mock
    private InternalApiClient apiClient;
    @Mock
    private ApiResponse<IndividualFullRecord> apiResponse;
    @Mock
    private PscFullRecordGet pscFullRecordGet;
    @Mock
    private PrivateDeltaResourceHandler deltaResourceHandler;
    @Mock
    private Transaction transaction;
    @Mock
    private Logger logger;

    private PscLookupService testService;

    @BeforeEach
    void setUp() {
        testService = new PscLookupServiceImpl(apiClientService, logger);
    }

    @Test
    void getPscIndividualWhenFound() throws IOException, URIValidationException {

        when(apiClientService.getInternalApiClient()).thenReturn(apiClient);
        when(apiClient.privatePscFullRecordResourceHandler()).thenReturn(deltaResourceHandler);

        when(deltaResourceHandler.getPscFullRecord(COMPANY
            + COMPANY_NUMBER
            + PERSONS_WITH_SIGNIFICANT_CONTROL
            + INDIVIDUAL.getValue()
            + "/"
            + PSC_ID
            + FULL_RECORD)).thenReturn(pscFullRecordGet);

        when(pscFullRecordGet.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(new IndividualFullRecord());

        var pscApi =
            testService.getIndividualFullRecord(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL);

        assertThat(pscApi, samePropertyValuesAs(new IndividualFullRecord()));

    }

    @Test
    void getPscWhenErrorRetrieving() throws IOException, URIValidationException {
        final var exception = new ApiErrorResponseException(
            new HttpResponseException.Builder(HttpStatusCodes.STATUS_CODE_FORBIDDEN, "test case",
                new HttpHeaders()));

        when(apiClientService.getInternalApiClient()).thenReturn(apiClient);
        when(apiClient.privatePscFullRecordResourceHandler()).thenReturn(deltaResourceHandler);

        when(deltaResourceHandler.getPscFullRecord(
            COMPANY
                + COMPANY_NUMBER
                + PERSONS_WITH_SIGNIFICANT_CONTROL
                + INDIVIDUAL.getValue()
                + "/"
                + PSC_ID
                + FULL_RECORD
        )).thenReturn(pscFullRecordGet);

        when(pscFullRecordGet.execute()).thenThrow(exception);

        final var thrown = assertThrows(PscLookupServiceException.class,
            () -> testService.getIndividualFullRecord(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL));

        assertThat(thrown.getMessage(), is("Error Retrieving PSC details for " + PSC_ID + ": 403 test case"));
    }

    @Test
    void getPscWhenURIErrorRetrieving() throws IOException, URIValidationException {
        final var exception = new URIValidationException("Incorrect URI");

        when(apiClientService.getInternalApiClient()).thenReturn(apiClient);
        when(apiClient.privatePscFullRecordResourceHandler()).thenReturn(deltaResourceHandler);

        when(deltaResourceHandler.getPscFullRecord(
            COMPANY
                + COMPANY_NUMBER
                + PERSONS_WITH_SIGNIFICANT_CONTROL
                + INDIVIDUAL.getValue()
                + "/"
                + PSC_ID
                + FULL_RECORD
        )).thenReturn(pscFullRecordGet);

        when(pscFullRecordGet.execute()).thenThrow(exception);

        final var thrown = assertThrows(PscLookupServiceException.class,
            () -> testService.getIndividualFullRecord(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL));

        assertThat(thrown.getMessage(), is("Error Retrieving PSC details for " + PSC_ID + ": Incorrect URI"));
    }

    @Test
    void getUvidMatchWithPscDataWhenNotFound() throws IOException, URIValidationException {

        final var exception = new ApiErrorResponseException(
            new HttpResponseException.Builder(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "test case",
                new HttpHeaders()));

        when(apiClientService.getInternalApiClient()).thenReturn(apiClient);
        when(apiClient.privatePscFullRecordResourceHandler()).thenReturn(deltaResourceHandler);

        when(deltaResourceHandler.getPscFullRecord(
            COMPANY
                + COMPANY_NUMBER
                + PERSONS_WITH_SIGNIFICANT_CONTROL
                + INDIVIDUAL.getValue()
                + "/"
                + PSC_ID
                + FULL_RECORD
        )).thenReturn(pscFullRecordGet);

        when(pscFullRecordGet.execute()).thenThrow(exception);

        final var thrown = assertThrows(FilingResourceNotFoundException.class,
            () -> testService.getIndividualFullRecord(transaction, PSC_VERIFICATION_DATA,
                INDIVIDUAL));

        assertThat(thrown.getMessage(),
            is("PSC Details not found for " + PSC_ID + ": 404 test case"));

    }

}