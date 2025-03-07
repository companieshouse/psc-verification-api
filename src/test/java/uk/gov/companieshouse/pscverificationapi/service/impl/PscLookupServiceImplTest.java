package uk.gov.companieshouse.pscverificationapi.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import uk.gov.companieshouse.api.model.psc.PscIndividualFullRecordApi;
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
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.psc.PscsResourceHandler;
import uk.gov.companieshouse.api.handler.psc.request.PscIndividualFullRecordGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.exception.PscLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.sdk.companieshouse.ApiClientService;
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
    private static final String CHS_INTERNAL_API_KEY = "key";

    @Mock
    private ApiClientService apiClientService;
    @Mock
    private ApiClient apiClient;
    @Mock
    private ApiResponse<PscIndividualFullRecordApi> apiResponse;
    @Mock
    private PscIndividualFullRecordGet pscIndividualFullRecordGet;
    @Mock
    private PscsResourceHandler pscResourceHandler;
    @Mock
    private Transaction transaction;
    @Mock
    private Logger logger;
    @Mock
    private EnvironmentReader environmentReader;

    private PscLookupService testService;

    @BeforeEach
    void setUp() {
        when(environmentReader.getMandatoryString("CHS_INTERNAL_API_KEY")).thenReturn("key");
        testService = new PscLookupServiceImpl(apiClientService, logger, environmentReader);
    }

    @Test
    void getPscIndividualWhenFound() throws IOException, URIValidationException {

        when(apiClientService.getApiClient(CHS_INTERNAL_API_KEY)).thenReturn(apiClient);
        when(apiClient.pscs()).thenReturn(pscResourceHandler);

        when(pscResourceHandler.getIndividualFullRecord("/company/"
            + COMPANY_NUMBER
            + "/persons-with-significant-control/"
            + INDIVIDUAL.getValue()
            + "/"
            + PSC_ID
            + "/full_record")).thenReturn(pscIndividualFullRecordGet);

        when(pscIndividualFullRecordGet.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(new PscIndividualFullRecordApi());

        var pscApi =
            testService.getPscIndividualFullRecord(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL);

        assertThat(pscApi, samePropertyValuesAs(new PscIndividualFullRecordApi()));

    }

    @Test
    void getPscWhenErrorRetrieving() throws IOException, URIValidationException {
        final var exception = new ApiErrorResponseException(
            new HttpResponseException.Builder(HttpStatusCodes.STATUS_CODE_FORBIDDEN, "test case",
                new HttpHeaders()));

        when(apiClientService.getApiClient(CHS_INTERNAL_API_KEY)).thenReturn(apiClient);
        when(apiClient.pscs()).thenReturn(pscResourceHandler);

        when(pscResourceHandler.getIndividualFullRecord(
            "/company/"
                + COMPANY_NUMBER
                + "/persons-with-significant-control/"
                + INDIVIDUAL.getValue()
                + "/"
                + PSC_ID
                + "/full_record"
        )).thenReturn(pscIndividualFullRecordGet);

        when(pscIndividualFullRecordGet.execute()).thenThrow(exception);

        final var thrown = assertThrows(PscLookupServiceException.class,
            () -> testService.getPscIndividualFullRecord(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL));

        assertThat(thrown.getMessage(), is("Error Retrieving PSC details for " + PSC_ID + ": 403 test case"));
    }

    @Test
    void getPscWhenURIErrorRetrieving() throws IOException, URIValidationException {
        final var exception = new URIValidationException("Incorrect URI");

        when(apiClientService.getApiClient(CHS_INTERNAL_API_KEY)).thenReturn(apiClient);
        when(apiClient.pscs()).thenReturn(pscResourceHandler);

        when(pscResourceHandler.getIndividualFullRecord(
            "/company/"
                + COMPANY_NUMBER
                + "/persons-with-significant-control/"
                + INDIVIDUAL.getValue()
                + "/"
                + PSC_ID
                + "/full_record"
        )).thenReturn(pscIndividualFullRecordGet);

        when(pscIndividualFullRecordGet.execute()).thenThrow(exception);

        final var thrown = assertThrows(PscLookupServiceException.class,
            () -> testService.getPscIndividualFullRecord(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL));

        assertThat(thrown.getMessage(), is("Error Retrieving PSC details for " + PSC_ID + ": Incorrect URI"));
    }

    @Test
    void getUvidMatchWithPscDataWhenNotFound() throws IOException, URIValidationException {

        final var exception = new ApiErrorResponseException(
            new HttpResponseException.Builder(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "test case",
                new HttpHeaders()));

        when(apiClientService.getApiClient(CHS_INTERNAL_API_KEY)).thenReturn(apiClient);
        when(apiClient.pscs()).thenReturn(pscResourceHandler);

        when(pscResourceHandler.getIndividualFullRecord(
            "/company/"
                + COMPANY_NUMBER
                + "/persons-with-significant-control/"
                + INDIVIDUAL.getValue()
                + "/"
                + PSC_ID
                + "/full_record"
        )).thenReturn(pscIndividualFullRecordGet);

        when(pscIndividualFullRecordGet.execute()).thenThrow(exception);

        final var thrown = assertThrows(FilingResourceNotFoundException.class,
            () -> testService.getPscIndividualFullRecord(transaction, PSC_VERIFICATION_DATA,
                INDIVIDUAL));

        assertThat(thrown.getMessage(),
            is("PSC Details not found for " + PSC_ID + ": 404 test case"));

    }

}