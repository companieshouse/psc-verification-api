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
import uk.gov.companieshouse.api.model.pscverification.NameMismatchReasonConstants;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.exception.IdvLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.service.IdvLookupService;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse.AccuracyStatementEnum.*;

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
    private Set<FieldError> errors;
    private String passthroughHeader;
    VerificationValidationContext validationContext;

    @BeforeEach
    void setUp() {

        errors = new HashSet<>();
        pscType = PscType.INDIVIDUAL;
        passthroughHeader = "passthroughHeader";
        validationContext = new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader);


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

    @Test
    void validateWhenNameMismatchNotNull() throws ApiErrorResponseException {

        when(pscVerificationData.verificationDetails()).thenReturn(verificationDetails);
        when(pscVerificationData.verificationDetails().nameMismatchReason()).thenReturn(NameMismatchReasonConstants.PREFERRED_NAME);
        when(pscLookupService.getUvidMatchWithPscData(transaction, pscVerificationData, pscType, passthroughHeader))
            .thenReturn(uvidMatch);
        when(idvLookupService.matchUvid(uvidMatch)).thenReturn(uvidMatchResponse);
        when(uvidMatchResponse.getAccuracyStatement()).thenReturn(Collections.singletonList(FORENAMES_MISMATCH));

        testValidator.validate(validationContext);
    }

    @ParameterizedTest
    @MethodSource("provideParameters")
    void validateWhenUvidMatchError(String errorValue, String errorText, List<UvidMatchResponse.AccuracyStatementEnum> statementEnums) throws ApiErrorResponseException {

        when(pscVerificationData.verificationDetails()).thenReturn(verificationDetails);
        when(pscVerificationData.verificationDetails().uvid()).thenReturn(errorValue);
        when(pscLookupService.getUvidMatchWithPscData(transaction, pscVerificationData, pscType, passthroughHeader))
            .thenReturn(uvidMatch);
        when(idvLookupService.matchUvid(uvidMatch)).thenReturn(uvidMatchResponse);
        when(uvidMatchResponse.getAccuracyStatement()).thenReturn(statementEnums);

        var fieldError = new FieldError("object", "uvid_match", pscVerificationData.verificationDetails().uvid(), false,
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
}