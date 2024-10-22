package uk.gov.companieshouse.pscverificationapi.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.pscverificationapi.enumerations.PscType.INDIVIDUAL;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.psc.PscsResourceHandler;
import uk.gov.companieshouse.api.handler.psc.request.PscIndividualGet;
import uk.gov.companieshouse.api.identityverification.model.UvidMatch;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.psc.NameElementsApi;
import uk.gov.companieshouse.api.model.psc.PscApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.exception.PscLookupServiceException;
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
            .pscAppointmentId(PSC_ID)
            .companyNumber(COMPANY_NUMBER)
            .verificationDetails(VERIFICATION_DETAILS)
            .build();
    private static final String[] FORENAMES = {"Forename1", "Forename2", "Forename3"};
    private static final String SURNAME = "Surname";
    private static final String PSC_FORENAME = "Forename1";
    private static final String PSC_MIDDLE_NAME = "Forename2 Forename3";
    private static final String PSC_SURNAME = "Surname";

    @Mock
    private ApiClientService apiClientService;
    @Mock
    private ApiClient apiClient;
    @Mock
    private ApiResponse<PscApi> apiResponse;
    @Mock
    private PscIndividualGet pscIndividualGet;
    @Mock
    private PscsResourceHandler pscsResourceHandler;
    @Mock
    private Transaction transaction;
    @Mock
    private Logger logger;
    private PscLookupService testService;

    @BeforeAll
    public static void setUpForClass() {
        PscType[] newEnumValues = addNewEnumValue();
        myMockedEnum = mockStatic(PscType.class);
        myMockedEnum.when(PscType::values).thenReturn(newEnumValues);
        mockedValue = newEnumValues[newEnumValues.length - 1];
        when(mockedValue.name()).thenReturn("UNKNOWN");
    }

    @AfterAll
    public static void tearDownForClass() {
        myMockedEnum.close();
    }

    @BeforeEach
    void setUp() {
        testService = new PscLookupServiceImpl(apiClientService, logger);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getPscIndividualWhenFound() throws IOException, URIValidationException {
        when(apiResponse.getData()).thenReturn(new PscApi());
        when(pscIndividualGet.execute()).thenReturn(apiResponse);
        when(pscsResourceHandler.getIndividual("/company/"
            + COMPANY_NUMBER
            + "/persons-with-significant-control/"
            + INDIVIDUAL.getValue()
            + "/"
            + PSC_ID)).thenReturn(pscIndividualGet);
        when(apiClient.pscs()).thenReturn(pscsResourceHandler);
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClient);

        var pscApi =
            testService.getPsc(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL, PASSTHROUGH_HEADER);

        assertThat(pscApi, samePropertyValuesAs(new PscApi()));

    }

    @Test
    void getPscWhenIoException() throws IOException {
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenThrow(
            new IOException("get test case"));

        final var thrown = assertThrows(PscLookupServiceException.class,
            () -> testService.getPsc(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL,
                PASSTHROUGH_HEADER));
        assertThat(thrown.getMessage(), is("Error Retrieving PSC details for " + PSC_ID + ": get test case"));
    }

    @Test
    void getPscWhenNotFound() throws IOException {
        final var exception = new ApiErrorResponseException(
            new HttpResponseException.Builder(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "test case",
                new HttpHeaders()));
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenThrow(exception);

        final var thrown = assertThrows(FilingResourceNotFoundException.class,
            () -> testService.getPsc(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL,
                PASSTHROUGH_HEADER));

        assertThat(thrown.getMessage(), is("PSC Details not found for " + PSC_ID + ": 404 test case"));
    }

    @Test
    void getPscWhenErrorRetrieving() throws IOException {
        final var exception = new ApiErrorResponseException(
            new HttpResponseException.Builder(HttpStatusCodes.STATUS_CODE_FORBIDDEN, "test case",
                new HttpHeaders()));
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenThrow(exception);

        final var thrown = assertThrows(PscLookupServiceException.class,
            () -> testService.getPsc(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL,
                PASSTHROUGH_HEADER));

        assertThat(thrown.getMessage(), is("Error Retrieving PSC details for " + PSC_ID + ": 403 test case"));
    }

    @Disabled("Disabled as RLE functionality is on hold")
    @Test
    void getPscWhenTypeNotRecognised() {

        final var thrown = assertThrows(UnsupportedOperationException.class,
            () -> testService.getPsc(transaction, PSC_VERIFICATION_DATA, mockedValue,
                PASSTHROUGH_HEADER));

        assertThat(thrown.getMessage(), is("PSC type UNKNOWN not supported for PSC ID " + PSC_ID));
    }

    @Test
    void getUvidMatchWithPscData() throws IOException, URIValidationException {

        UvidMatch expected = createUvid(UVID_CODE);
        setNames(expected, FORENAMES, SURNAME);
        final var pscData = createPscData(PSC_FORENAME, PSC_MIDDLE_NAME, PSC_SURNAME);

        when(apiResponse.getData()).thenReturn(pscData);
        when(pscIndividualGet.execute()).thenReturn(apiResponse);
        when(pscsResourceHandler.getIndividual("/company/"
            + COMPANY_NUMBER
            + "/persons-with-significant-control/"
            + INDIVIDUAL.getValue()
            + "/"
            + PSC_ID)).thenReturn(pscIndividualGet);
        when(apiClient.pscs()).thenReturn(pscsResourceHandler);
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClient);

        var testUvid =
            testService.getUvidMatchWithPscData(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL,
                PASSTHROUGH_HEADER);

        assertThat("Forenames order does not match", testUvid.getForenames(), is(equalTo(expected.getForenames())));
        assertThat(testUvid, samePropertyValuesAs(expected));
    }

    @Test
    void getUvidMatchWithNoUvid() throws IOException, URIValidationException {

        VerificationDetails verificationDetails = VerificationDetails.newBuilder().build();
        PscVerificationData pscVerificationData =
            PscVerificationData.newBuilder()
                .pscAppointmentId(PSC_ID)
                .companyNumber(COMPANY_NUMBER)
                .verificationDetails(verificationDetails)
                .build();
        UvidMatch expected = createUvid("");
        setNames(expected, FORENAMES, SURNAME);
        final var pscData = createPscData(PSC_FORENAME, PSC_MIDDLE_NAME, PSC_SURNAME);

        when(apiResponse.getData()).thenReturn(pscData);
        when(pscIndividualGet.execute()).thenReturn(apiResponse);
        when(pscsResourceHandler.getIndividual("/company/"
            + COMPANY_NUMBER
            + "/persons-with-significant-control/"
            + INDIVIDUAL.getValue()
            + "/"
            + PSC_ID)).thenReturn(pscIndividualGet);
        when(apiClient.pscs()).thenReturn(pscsResourceHandler);
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClient);

        var testUvid =
            testService.getUvidMatchWithPscData(transaction, pscVerificationData, INDIVIDUAL,
                PASSTHROUGH_HEADER);

        assertThat("Forenames order does not match", testUvid.getForenames(), is(equalTo(expected.getForenames())));
        assertThat(testUvid, samePropertyValuesAs(expected));

    }

    @Test
    void getUvidMatchWithPscDataWhenForenamesBlank()
        throws IOException, URIValidationException {

        UvidMatch expected = createUvid(UVID_CODE);
        setNames(expected, new ArrayList<String>(List.of()).toArray(new String[0]), SURNAME);
        final var pscData = createPscData(null, null, PSC_SURNAME);

        when(apiResponse.getData()).thenReturn(pscData);
        when(pscIndividualGet.execute()).thenReturn(apiResponse);
        when(pscsResourceHandler.getIndividual("/company/"
            + COMPANY_NUMBER
            + "/persons-with-significant-control/"
            + INDIVIDUAL.getValue()
            + "/"
            + PSC_ID)).thenReturn(pscIndividualGet);
        when(apiClient.pscs()).thenReturn(pscsResourceHandler);
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClient);

        var testUvid =
            testService.getUvidMatchWithPscData(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL,
                PASSTHROUGH_HEADER);

        assertThat("Forenames order does not match", testUvid.getForenames(), is(equalTo(expected.getForenames())));
        assertThat(testUvid, samePropertyValuesAs(expected));

    }

    @Test
    void getUvidMatchWithPscDataWhenSurnameBlank() throws IOException, URIValidationException {

        UvidMatch expected = createUvid(UVID_CODE);
        setNames(expected, FORENAMES, "");
        final var pscData = createPscData(PSC_FORENAME, PSC_MIDDLE_NAME, "");

        when(apiResponse.getData()).thenReturn(pscData);
        when(pscIndividualGet.execute()).thenReturn(apiResponse);
        when(pscsResourceHandler.getIndividual("/company/"
            + COMPANY_NUMBER
            + "/persons-with-significant-control/"
            + INDIVIDUAL.getValue()
            + "/"
            + PSC_ID)).thenReturn(pscIndividualGet);
        when(apiClient.pscs()).thenReturn(pscsResourceHandler);
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClient);

        var testUvid =
            testService.getUvidMatchWithPscData(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL,
                PASSTHROUGH_HEADER);

        assertThat("Forenames order does not match", testUvid.getForenames(),
            is(equalTo(expected.getForenames())));
        assertThat(testUvid, samePropertyValuesAs(expected));

    }

    @Test
    void getUvidMatchWithPscDataWhenNotFound() throws IOException {

        final var exception = new ApiErrorResponseException(
            new HttpResponseException.Builder(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "test case",
                new HttpHeaders()));

        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenThrow(exception);

        final var thrown = assertThrows(FilingResourceNotFoundException.class,
            () -> testService.getUvidMatchWithPscData(transaction, PSC_VERIFICATION_DATA,
                INDIVIDUAL, PASSTHROUGH_HEADER));

        assertThat(thrown.getMessage(),
            is("PSC Details not found for " + PSC_ID + ": 404 test case"));

    }

    @Test
    void getUvidMatchWithPscDataWhenErrorRetrieving() throws IOException {

        final var exception = new ApiErrorResponseException(
            new HttpResponseException.Builder(HttpStatusCodes.STATUS_CODE_FORBIDDEN, "test case",
                new HttpHeaders()));
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenThrow(exception);

        final var thrown = assertThrows(PscLookupServiceException.class,
            () -> testService.getUvidMatchWithPscData(transaction, PSC_VERIFICATION_DATA,
                INDIVIDUAL, PASSTHROUGH_HEADER));

        assertThat(thrown.getMessage(),
            is("Error Retrieving PSC details for " + PSC_ID + ": 403 test case"));

    }

    private static UvidMatch createUvid(String uvidCode) {
        UvidMatch uvidMatch = new UvidMatch();
        uvidMatch.setUvid(uvidCode);
        return uvidMatch;
    }

    private static void setNames(final UvidMatch uvidMatch, String[] forenames, String surname) {
        List<String> forenamesAsList = Optional.ofNullable(forenames)
            .map(Arrays::asList)
            .map(ArrayList::new)
            .orElseGet(ArrayList::new);

        uvidMatch.setForenames(forenamesAsList);
        uvidMatch.setSurname(surname);
    }

    private static PscApi createPscData(String forename, String middleName, String surname) {
        PscApi pscData = new PscApi();
        NameElementsApi nameElements = new NameElementsApi();
        nameElements.setForename(forename);
        nameElements.setMiddleName(middleName);
        nameElements.setSurname(surname);
        pscData.setNameElements(nameElements);
        return pscData;
    }
}