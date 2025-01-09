package uk.gov.companieshouse.pscverificationapi.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
import uk.gov.companieshouse.api.identityverification.model.UvidMatch;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.psc.IndividualFullRecord;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.psc.DateOfBirth;
import uk.gov.companieshouse.api.psc.NameElements;
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
            .pscAppointmentId(PSC_ID)
            .companyNumber(COMPANY_NUMBER)
            .verificationDetails(VERIFICATION_DETAILS)
            .build();
    private static final String[] FORENAMES = {"Forename1", "Forename2 Forename3"};
    private static final String SURNAME = "Surname";
    private static final String PSC_FORENAME = "Forename1";
    private static final String PSC_MIDDLE_NAME = "Forename2 Forename3";
    private static final String PSC_SURNAME = "Surname";
    private static final DateOfBirth DATE_OF_BIRTH = new DateOfBirth(2, 1983).day(27);
    private static final String CHS_INTERNAL_API_KEY = "key";

    @Mock
    private ApiClientService apiClientService;
    @Mock
    private ApiClient apiClient;
    @Mock
    private ApiResponse<IndividualFullRecord> apiResponse;
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
        when(apiResponse.getData()).thenReturn(new IndividualFullRecord());

        var pscApi =
            testService.getPscIndividualFullRecord(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL);

        assertThat(pscApi, samePropertyValuesAs(new IndividualFullRecord()));

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
    void getUvidMatchWithPscData() throws IOException, URIValidationException {

        UvidMatch expected = createUvid(UVID_CODE);
        setNames(expected, FORENAMES, SURNAME);

        String stringDateOfBirth = String.format("%04d-%02d-%02d", 1983, 2, 27);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        expected.setDateOfBirth(LocalDate.parse(stringDateOfBirth, formatter));

        final var pscData = createPscData(PSC_FORENAME, PSC_MIDDLE_NAME, PSC_SURNAME, DATE_OF_BIRTH);

        when(apiResponse.getData()).thenReturn(pscData);
        when(pscIndividualFullRecordGet.execute()).thenReturn(apiResponse);
        when(pscResourceHandler.getIndividualFullRecord("/company/"
            + COMPANY_NUMBER
            + "/persons-with-significant-control/"
            + INDIVIDUAL.getValue()
            + "/"
            + PSC_ID
            + "/full_record")).thenReturn(pscIndividualFullRecordGet);
        when(apiClientService.getApiClient(CHS_INTERNAL_API_KEY)).thenReturn(apiClient);
        when(apiClientService.getApiClient(CHS_INTERNAL_API_KEY).pscs()).thenReturn(pscResourceHandler);

        var testUvid =
            testService.getUvidMatchWithPscData(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL);

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
        expected.setDateOfBirth(LocalDate.of(1983, 2, 27));
        final var pscData = createPscData(PSC_FORENAME, PSC_MIDDLE_NAME, PSC_SURNAME, DATE_OF_BIRTH);

        when(apiResponse.getData()).thenReturn(pscData);
        when(pscIndividualFullRecordGet.execute()).thenReturn(apiResponse);
        when(pscResourceHandler.getIndividualFullRecord("/company/"
            + COMPANY_NUMBER
            + "/persons-with-significant-control/"
            + INDIVIDUAL.getValue()
            + "/"
            + PSC_ID
            + "/full_record")).thenReturn(pscIndividualFullRecordGet);
        when(apiClientService.getApiClient(CHS_INTERNAL_API_KEY)).thenReturn(apiClient);
        when(apiClientService.getApiClient(CHS_INTERNAL_API_KEY).pscs()).thenReturn(pscResourceHandler);

        var testUvid =
            testService.getUvidMatchWithPscData(transaction, pscVerificationData, INDIVIDUAL);

        assertThat("Forenames order does not match", testUvid.getForenames(), is(equalTo(expected.getForenames())));
        assertThat(testUvid, samePropertyValuesAs(expected));

    }

    @Test
    void getUvidMatchWithPscDataWhenForenamesBlank()
        throws IOException, URIValidationException {

        UvidMatch expected = createUvid(UVID_CODE);
        setNames(expected, new ArrayList<String>(List.of()).toArray(new String[0]), SURNAME);
        String stringDateOfBirth = String.format("%04d-%02d-%02d", 1983, 2, 27);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        expected.setDateOfBirth(LocalDate.parse(stringDateOfBirth, formatter));
        final var pscData = createPscData(null, null, PSC_SURNAME, DATE_OF_BIRTH);

        when(apiResponse.getData()).thenReturn(pscData);
        when(pscIndividualFullRecordGet.execute()).thenReturn(apiResponse);
        when(pscResourceHandler.getIndividualFullRecord("/company/"
            + COMPANY_NUMBER
            + "/persons-with-significant-control/"
            + INDIVIDUAL.getValue()
            + "/"
            + PSC_ID
            + "/full_record")).thenReturn(pscIndividualFullRecordGet);
        when(apiClientService.getApiClient(CHS_INTERNAL_API_KEY)).thenReturn(apiClient);
        when(apiClientService.getApiClient(CHS_INTERNAL_API_KEY).pscs()).thenReturn(pscResourceHandler);

        var testUvid =
            testService.getUvidMatchWithPscData(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL);

        assertThat("Forenames order does not match", testUvid.getForenames(), is(equalTo(expected.getForenames())));
        assertThat(testUvid, samePropertyValuesAs(expected));

    }

    @Test
    void getUvidMatchWithPscDataWhenSurnameBlank() throws IOException, URIValidationException {

        UvidMatch expected = createUvid(UVID_CODE);
        setNames(expected, FORENAMES, "");
        String stringDateOfBirth = String.format("%04d-%02d-%02d", 1983, 2, 27);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        expected.setDateOfBirth(LocalDate.parse(stringDateOfBirth, formatter));
        final var pscData = createPscData(PSC_FORENAME, PSC_MIDDLE_NAME, "", DATE_OF_BIRTH);

        when(apiResponse.getData()).thenReturn(pscData);
        when(pscIndividualFullRecordGet.execute()).thenReturn(apiResponse);
        when(pscResourceHandler.getIndividualFullRecord("/company/"
            + COMPANY_NUMBER
            + "/persons-with-significant-control/"
            + INDIVIDUAL.getValue()
            + "/"
            + PSC_ID
            + "/full_record")).thenReturn(pscIndividualFullRecordGet);
        when(apiClientService.getApiClient(CHS_INTERNAL_API_KEY)).thenReturn(apiClient);
        when(apiClientService.getApiClient(CHS_INTERNAL_API_KEY).pscs()).thenReturn(pscResourceHandler);

        var testUvid =
            testService.getUvidMatchWithPscData(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL);

        assertThat("Forenames order does not match", testUvid.getForenames(),
            is(equalTo(expected.getForenames())));
        assertThat(testUvid, samePropertyValuesAs(expected));

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
            () -> testService.getUvidMatchWithPscData(transaction, PSC_VERIFICATION_DATA,
                INDIVIDUAL));

        assertThat(thrown.getMessage(),
            is("PSC Details not found for " + PSC_ID + ": 404 test case"));

    }

    @Test
    void getUvidMatchWithPscDataWhenErrorRetrieving() throws IOException, URIValidationException {

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
            () -> testService.getUvidMatchWithPscData(transaction, PSC_VERIFICATION_DATA,
                INDIVIDUAL));

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

    private static IndividualFullRecord createPscData(String forename, String middleName, String surname, DateOfBirth dateOfBirth) {
        IndividualFullRecord individualFullRecord = new IndividualFullRecord();
        NameElements nameElements = new NameElements();
        nameElements.setForename(forename);
        nameElements.setMiddleName(middleName);
        nameElements.setSurname(surname);
        individualFullRecord.setNameElements(nameElements);

        individualFullRecord.setDateOfBirth(dateOfBirth);

        return individualFullRecord;
    }
}