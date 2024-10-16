package uk.gov.companieshouse.pscverificationapi.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private static final VerificationDetails verificationDetails =
        VerificationDetails.newBuilder().uvid("XY222222223")
            .build();
    private static final PscVerificationData PSC_VERIFICATION_DATA =
        PscVerificationData.newBuilder()
            .pscAppointmentId(PSC_ID)
            .companyNumber(COMPANY_NUMBER)
            .verificationDetails(verificationDetails)
            .build();

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
        assertThat(thrown.getMessage(),
            is("Error Retrieving PSC details for " + PSC_ID + ": get test case"));
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

        assertThat(thrown.getMessage(),
            is("PSC Details not found for " + PSC_ID + ": 404 test case"));
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

        assertThat(thrown.getMessage(),
            is("Error Retrieving PSC details for " + PSC_ID + ": 403 test case"));
    }

    //TODO - Add if RO verifications implemented
    @Disabled
    @Test
    void getPscWhenTypeNotRecognised() {

        final var thrown = assertThrows(UnsupportedOperationException.class,
            () -> testService.getPsc(transaction, PSC_VERIFICATION_DATA, mockedValue,
                PASSTHROUGH_HEADER));

        assertThat(thrown.getMessage(), is("PSC type UNKNOWN not supported for PSC ID " + PSC_ID));
    }

    @Test
    void getUvidMatchWithPscData() throws IOException, URIValidationException {

        UvidMatch uvidMatch = new UvidMatch();
        uvidMatch.setUvid("XY222222223");
        List<String> forenames =
            new ArrayList<>(Arrays.asList("Forename1", "Forename2", "Forename3"));
        uvidMatch.setForenames(forenames);
        uvidMatch.setSurname("Surname");

        PscApi pscData = new PscApi();
        NameElementsApi nameElements = new NameElementsApi();
        nameElements.setForename("Forename1");
        nameElements.setMiddleName("Forename2 Forename3");
        nameElements.setSurname("Surname");
        pscData.setNameElements(nameElements);

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

        var uvidMatchTest =
            testService.getUvidMatchWithPscData(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL,
                PASSTHROUGH_HEADER);

        assertThat(uvidMatchTest, samePropertyValuesAs(uvidMatch));
        //verify the forenames are in the correct order
        assertEquals(uvidMatchTest.getForenames(), uvidMatch.getForenames());

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

        UvidMatch uvidMatch = new UvidMatch();
        List<String> forenames =
            new ArrayList<>(Arrays.asList("Forename1", "Forename2", "Forename3"));
        uvidMatch.setForenames(forenames);
        uvidMatch.setSurname("Surname");
        uvidMatch.setUvid("");

        PscApi pscData = new PscApi();
        NameElementsApi nameElements = new NameElementsApi();
        nameElements.setForename("Forename1");
        nameElements.setMiddleName("Forename2 Forename3");
        nameElements.setSurname("Surname");
        pscData.setNameElements(nameElements);

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

        var uvidMatchTest =
            testService.getUvidMatchWithPscData(transaction, pscVerificationData, INDIVIDUAL,
                PASSTHROUGH_HEADER);

        assertThat(uvidMatchTest, samePropertyValuesAs(uvidMatch));
        //verify the forenames are in the correct order
        assertEquals(uvidMatchTest.getForenames(), uvidMatch.getForenames());

    }

    @Test
    void getUvidMatchWithPscDataWhenForenamesBlank()
        throws IOException, URIValidationException {

        UvidMatch uvidMatch = new UvidMatch();
        uvidMatch.setUvid("XY222222223");
        List<String> forenames = new ArrayList<>(List.of());
        uvidMatch.setForenames(forenames);
        uvidMatch.setSurname("Surname");

        PscApi pscData = new PscApi();
        NameElementsApi nameElements = new NameElementsApi();
        nameElements.setSurname("Surname");
        pscData.setNameElements(nameElements);

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

        var uvidMatchTest =
            testService.getUvidMatchWithPscData(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL,
                PASSTHROUGH_HEADER);

        assertThat(uvidMatchTest, samePropertyValuesAs(uvidMatch));
        //verify the forenames are in the correct order
        assertEquals(uvidMatchTest.getForenames(), uvidMatch.getForenames());

    }

    @Test
    void getUvidMatchWithPscDataWhenSurnameBlank() throws IOException, URIValidationException {

        UvidMatch uvidMatch = new UvidMatch();
        uvidMatch.setUvid("XY222222223");
        List<String> forenames =
            new ArrayList<>(Arrays.asList("Forename1", "Forename2", "Forename3"));
        uvidMatch.setForenames(forenames);
        uvidMatch.setSurname("");

        PscApi pscData = new PscApi();
        NameElementsApi nameElements = new NameElementsApi();
        nameElements.setForename("Forename1");
        nameElements.setMiddleName("Forename2 Forename3");
        pscData.setNameElements(nameElements);

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

        var uvidMatchTest =
            testService.getUvidMatchWithPscData(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL,
                PASSTHROUGH_HEADER);

        assertThat(uvidMatchTest, samePropertyValuesAs(uvidMatch));
        assertEquals(uvidMatchTest.getForenames(), uvidMatch.getForenames());

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

    @Disabled
    @Test
    void getUvidMatchWithPscDataWhenTypeNotRecognised() {
        //TODO - Add if RO verifications implemented
    }
}