package uk.gov.companieshouse.pscverificationapi.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

@ExtendWith(MockitoExtension.class)
class PscExistsValidatorTest {

    @Mock
    private PscLookupService pscLookupService;
    @Mock
    private PscVerificationData pscVerificationData;
    @Mock
    private Map<String, String> validation;
    @Mock
    private Transaction transaction;
    @Mock
    private ApiErrorResponseException errorResponseException;

    PscExistsValidator testValidator;
    private PscType pscType;
    private List<FieldError> errors;
    private String passthroughHeader;

    private static final String PSC_ID = "67edfE436y35hetsie6zuAZtr";

    @BeforeEach
    void setUp() {

        errors = new ArrayList<>();
        pscType = PscType.INDIVIDUAL;
        passthroughHeader = "passthroughHeader";

        testValidator = new PscExistsValidator(validation, pscLookupService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void validateWhenPscExists() {

        boolean isValid = testValidator.validate(
            new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors, is(empty()));
        assertThat(isValid, is(true));

    }

    @Test
    void validateWhenPscDoesNotExist() {
        var fieldError = new FieldError("object", "psc_appointment_id", PSC_ID, false,
            new String[]{null, PSC_ID}, null,
            "not-exists default message");
        when(pscVerificationData.pscAppointmentId()).thenReturn(PSC_ID);
        when(pscLookupService.getPsc(transaction, pscVerificationData, pscType,
            passthroughHeader)).thenThrow(new FilingResourceNotFoundException(
            "PSC Details not found for " + PSC_ID + ": 404 Not Found", errorResponseException));
        when(validation.get("psc_appointment_id-not-found")).thenReturn("not-exists default message");

        boolean isValid = testValidator.validate(
            new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors.stream().findFirst().orElseThrow(), equalTo(fieldError));
        assertThat(errors, contains(fieldError));
        assertThat(isValid, is(false));
    }

}