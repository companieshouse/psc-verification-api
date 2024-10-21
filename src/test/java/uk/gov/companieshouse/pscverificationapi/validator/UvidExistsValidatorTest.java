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
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.exception.IdvLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.service.IdvLookupService;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse.AccuracyStatementEnum.DETAILS_MATCH_UVID;
import static uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse.AccuracyStatementEnum.DOB_MISMATCH;
import static uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse.AccuracyStatementEnum.EXPIRED_UVID;
import static uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse.AccuracyStatementEnum.FORENAMES_MISMATCH;
import static uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse.AccuracyStatementEnum.INVALID_RECORD;
import static uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse.AccuracyStatementEnum.SURNAME_MISMATCH;
import static uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse.AccuracyStatementEnum.UNKNOWN_UVID;

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

    @Mock
    private VerificationDetails verificationDetails;

    @Mock
    private UvidMatch uvidMatch;

    UvidExistsValidator testValidator;
    private PscType pscType;
    private List<FieldError> errors;
    private String passthroughHeader;

    @BeforeEach
    void setUp() {

        errors = new ArrayList<>();
        pscType = PscType.INDIVIDUAL;
        passthroughHeader = "passthroughHeader";

        testValidator = new UvidExistsValidator(validation, idvLookupService, pscLookupService);
    }

    @Test
    void validateWhenUvidMatch() throws ApiErrorResponseException {

        when(pscLookupService.getUvidMatchWithPscData(transaction, pscVerificationData, pscType, passthroughHeader))
            .thenReturn(uvidMatch);
        when(idvLookupService.matchUvid(uvidMatch)).thenReturn(uvidMatchResponse);
        when(uvidMatchResponse.getAccuracyStatement()).thenReturn(Collections.singletonList(DETAILS_MATCH_UVID));

        testValidator.validate(
            new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader)
        );
        assertThat(errors, is(empty()));
    }

    @Test
    void validateWhenException() throws ApiErrorResponseException {

        when(pscVerificationData.verificationDetails()).thenReturn(verificationDetails);
        when(pscVerificationData.verificationDetails().uvid()).thenReturn("1234567890");
        when(pscLookupService.getUvidMatchWithPscData(transaction, pscVerificationData, pscType, passthroughHeader))
            .thenReturn(uvidMatch);
        when(idvLookupService.matchUvid(uvidMatch)).thenThrow(new ApiErrorResponseException(
                new HttpResponseException.Builder(400, "test error", new HttpHeaders())));

        final var exception = Assertions.assertThrows(IdvLookupServiceException.class,
            () -> testValidator.validate(new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader)));
        assertThat(exception.getMessage(), is("Error matching UVID 1234567890: null test error"));
    }

    @ParameterizedTest
    @MethodSource("provideParameters")
    void validateWhenUvidMatchError(UvidMatchResponse.AccuracyStatementEnum accuracyStatementEnum, String errorValue, String errorText) throws ApiErrorResponseException {

        when(pscVerificationData.verificationDetails()).thenReturn(verificationDetails);
        when(pscVerificationData.verificationDetails().uvid()).thenReturn(errorValue);
        when(pscLookupService.getUvidMatchWithPscData(transaction, pscVerificationData, pscType, passthroughHeader))
            .thenReturn(uvidMatch);
        when(idvLookupService.matchUvid(uvidMatch)).thenReturn(uvidMatchResponse);
        when(uvidMatchResponse.getAccuracyStatement()).thenReturn(Collections.singletonList(accuracyStatementEnum));

        var fieldError = new FieldError("object", "uvid_match", pscVerificationData.verificationDetails().uvid(), false,
            new String[]{null, errorValue}, null, errorText);

        when(validation.get(errorValue)).thenReturn(errorText);

        testValidator.validate(
            new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors.stream().findFirst().orElseThrow(), equalTo(fieldError));
        assertThat(errors, contains(fieldError));
    }

    private static Stream<Arguments> provideParameters() {

        return Stream.of(
            Arguments.of(UNKNOWN_UVID, "unknown-uvid", "UVID is not recognised"),
            Arguments.of(EXPIRED_UVID, "expired-uvid", "UVID has expired"),
            Arguments.of(INVALID_RECORD, "invalid-record", "UVID is invalid"),
            Arguments.of(DOB_MISMATCH, "dob-mismatch", "Date of birth does not match"),
            Arguments.of(FORENAMES_MISMATCH, "forenames-mismatch",
                "Forename(s) do not match: a name mismatch reason must be provided"),
            Arguments.of(SURNAME_MISMATCH, "surname-mismatch",
                "Surname does not match: a name mismatch reason must be provided"));
    }
}