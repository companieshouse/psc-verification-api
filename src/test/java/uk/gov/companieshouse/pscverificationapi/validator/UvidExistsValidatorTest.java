package uk.gov.companieshouse.pscverificationapi.validator;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.identityverification.model.UvidMatch;
import uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse;
import uk.gov.companieshouse.api.model.psc.IndividualFullRecord;
import uk.gov.companieshouse.api.model.pscverification.NameMismatchReasonConstants;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.psc.DateOfBirth;
import uk.gov.companieshouse.api.psc.NameElements;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.exception.IdvLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.service.IdvLookupService;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse.AccuracyStatementEnum.*;
import static uk.gov.companieshouse.pscverificationapi.enumerations.PscType.INDIVIDUAL;

@ExtendWith(MockitoExtension.class)
class UvidExistsValidatorTest {

    @Mock
    private Map<String, String> validation;

    @Mock
    private IdvLookupService idvLookupService;

    @Mock
    private PscLookupService pscLookupService;

    @Mock
    private PscVerificationData pscVerificationData;

    @Mock
    private Transaction transaction;

    @Mock
    private UvidMatchResponse uvidMatchResponse;

    UvidExistsValidator testValidator;
    private PscType pscType;
    private Set<FieldError> errors;
    private String passthroughHeader;
    VerificationValidationContext validationContext;

    private static final String PSC_ID = "654321";
    private static final String UVID_CODE = "XY222222223";
    private static final String COMPANY_NUMBER = "12345678";
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

    @BeforeEach
    void setUp() {

        errors = new HashSet<>();
        pscType = INDIVIDUAL;
        passthroughHeader = "passthroughHeader";
        validationContext = new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader);


        testValidator = new UvidExistsValidator(validation, idvLookupService, pscLookupService);
    }

    @Test
    void validateWhenException() throws ApiErrorResponseException {

        UvidMatch expected = createUvid(UVID_CODE);
        setNames(expected, FORENAMES, SURNAME);

        String stringDateOfBirth = String.format("%04d-%02d-%02d", 1983, 2, 27);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        expected.setDateOfBirth(LocalDate.parse(stringDateOfBirth, formatter));

        final var pscData = createPscData(PSC_FORENAME, PSC_MIDDLE_NAME, PSC_SURNAME, DATE_OF_BIRTH);

        when(pscLookupService.getPscIndividualFullRecord(transaction, PSC_VERIFICATION_DATA, pscType)).thenReturn(pscData);
        when(idvLookupService.matchUvid(expected)).thenThrow(new ApiErrorResponseException(
                new HttpResponseException.Builder(400, "test error", new HttpHeaders())));

        final var exception = Assertions.assertThrows(IdvLookupServiceException.class,
            () -> testValidator.validate(new VerificationValidationContext(PSC_VERIFICATION_DATA, errors, transaction, pscType, passthroughHeader)));
        assertThat(exception.getMessage(), is("Error matching UVID XY222222223: null test error"));
    }

    @Test
    void validateWhenNameMismatchNotNull() throws ApiErrorResponseException {

        UvidMatch expected = createUvid(UVID_CODE);
        setNames(expected, FORENAMES, SURNAME);

        String stringDateOfBirth = String.format("%04d-%02d-%02d", 1983, 2, 27);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        expected.setDateOfBirth(LocalDate.parse(stringDateOfBirth, formatter));

        var verificationDetails = VerificationDetails.newBuilder(PSC_VERIFICATION_DATA.verificationDetails()).nameMismatchReason(NameMismatchReasonConstants.PREFERRED_NAME).build();

        final var individualFullRecord = createPscData(PSC_FORENAME, PSC_MIDDLE_NAME, PSC_SURNAME, DATE_OF_BIRTH);

        when(pscLookupService.getPscIndividualFullRecord(transaction, pscVerificationData, pscType)).thenReturn(individualFullRecord);
        when(pscVerificationData.verificationDetails()).thenReturn(verificationDetails);
        when(idvLookupService.matchUvid(expected)).thenReturn(uvidMatchResponse);
        when(uvidMatchResponse.getAccuracyStatement()).thenReturn(Collections.singletonList(FORENAMES_MISMATCH));

        testValidator.validate(validationContext);
    }

    @Test
    void validateWhenUvidMatch() throws ApiErrorResponseException {

        UvidMatch expected = createUvid(UVID_CODE);
        setNames(expected, FORENAMES, SURNAME);

        String stringDateOfBirth = String.format("%04d-%02d-%02d", 1983, 2, 27);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        expected.setDateOfBirth(LocalDate.parse(stringDateOfBirth, formatter));

        final var individualFullRecord = createPscData(PSC_FORENAME, PSC_MIDDLE_NAME, PSC_SURNAME, DATE_OF_BIRTH);
        when(pscLookupService.getPscIndividualFullRecord(transaction, pscVerificationData, pscType)).thenReturn(individualFullRecord);

        when(pscVerificationData.verificationDetails()).thenReturn(VERIFICATION_DETAILS);
        when(idvLookupService.matchUvid(expected)).thenReturn(uvidMatchResponse);
        when(uvidMatchResponse.getAccuracyStatement()).thenReturn(Collections.singletonList(DETAILS_MATCH_UVID));

        testValidator.validate(validationContext);
    }

    @Test
    void getUvidMatchWithPscDataNoForename() throws ApiErrorResponseException {

        UvidMatch expected = createUvid(UVID_CODE);
        setNames(expected, null, SURNAME);

        String stringDateOfBirth = String.format("%04d-%02d-%02d", 1983, 2, 27);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        expected.setDateOfBirth(LocalDate.parse(stringDateOfBirth, formatter));

        final var pscData = createPscData(null, null, PSC_SURNAME, DATE_OF_BIRTH);

        when(pscLookupService.getPscIndividualFullRecord(transaction, PSC_VERIFICATION_DATA, pscType)).thenReturn(pscData);

        var testUvid = testValidator.getUvidMatchWithPscData(transaction, PSC_VERIFICATION_DATA, INDIVIDUAL);

        assertThat("Forenames order does not match", testUvid.getForenames(), is(equalTo(expected.getForenames())));
        assertThat(testUvid, samePropertyValuesAs(expected));

        when(idvLookupService.matchUvid(expected)).thenReturn(uvidMatchResponse);
        when(uvidMatchResponse.getAccuracyStatement()).thenReturn(Collections.singletonList(DETAILS_MATCH_UVID));

        testValidator.validate(
            new VerificationValidationContext(PSC_VERIFICATION_DATA, errors, transaction, pscType, passthroughHeader)
        );
        assertThat(errors, is(empty()));
    }

    @ParameterizedTest
    @MethodSource("provideParameters")
    void validateWhenUvidMatchError(String errorValue, String errorText, List<UvidMatchResponse.AccuracyStatementEnum> statementEnums) throws ApiErrorResponseException {

        UvidMatch expected = createUvid(UVID_CODE);
        setNames(expected, FORENAMES, SURNAME);

        String stringDateOfBirth = String.format("%04d-%02d-%02d", 1983, 2, 27);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        expected.setDateOfBirth(LocalDate.parse(stringDateOfBirth, formatter));

        final var individualFullRecord = createPscData(PSC_FORENAME, PSC_MIDDLE_NAME, PSC_SURNAME, DATE_OF_BIRTH);
        when(pscLookupService.getPscIndividualFullRecord(transaction, pscVerificationData, pscType)).thenReturn(individualFullRecord);

        when(pscVerificationData.verificationDetails()).thenReturn(VERIFICATION_DETAILS);
        when(idvLookupService.matchUvid(expected)).thenReturn(uvidMatchResponse);
        when(uvidMatchResponse.getAccuracyStatement()).thenReturn(statementEnums);

        var fieldError = new FieldError("object", "uvid_match", PSC_VERIFICATION_DATA.verificationDetails().uvid(), false,
            new String[]{null, errorValue}, null, errorText);

        when(validation.get(errorValue)).thenReturn(errorText);

        testValidator.validate(validationContext);

        assertThat(errors.stream().findFirst().orElseThrow(), equalTo(fieldError));
        assertThat(errors, contains(fieldError));
    }

    private static Stream<Arguments> provideParameters() {

        return Stream.of(
            Arguments.of("unknown-uvid", "UVID is not recognised", List.of(UNKNOWN_UVID)),
            Arguments.of("expired-uvid", "UVID has expired", List.of(EXPIRED_UVID)),
            Arguments.of("invalid-record", "UVID is invalid", List.of(INVALID_RECORD)),
            Arguments.of("dob-mismatch", "Date of birth does not match", List.of(DOB_MISMATCH)),
            Arguments.of("no-name-mismatch-reason",
                "The name on the public register is different to the name this PSC used for identity verification:" +
                    " a name mismatch reason must be provided", List.of(FORENAMES_MISMATCH)),
            Arguments.of("no-name-mismatch-reason",
                "The name on the public register is different to the name this PSC used for identity verification:" +
                    " a name mismatch reason must be provided", List.of(SURNAME_MISMATCH)),
            Arguments.of("no-name-mismatch-reason",
                "The name on the public register is different to the name this PSC used for identity verification:" +
                    " a name mismatch reason must be provided", List.of(FORENAMES_MISMATCH, SURNAME_MISMATCH)));
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