package uk.gov.companieshouse.pscverificationapi.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;

@ExtendWith(MockitoExtension.class)
class PscIdProvidedValidatorTest {

    @Mock
    VerificationDetails verificationDetails;
    @Mock
    private Map<String, String> validation;
    @Mock
    private Transaction transaction;

    PscIdProvidedValidator testValidator;
    private PscType pscType;
    private Set<FieldError> errors;
    private String passthroughHeader;
    private PscVerificationData pscVerificationData;

    private static final String PSC_ID = "67edfE436y35hetsie6zuAZtr";

    @BeforeEach
    void setUp() {

        errors = new HashSet<>();
        pscType = PscType.INDIVIDUAL;
        passthroughHeader = "passthroughHeader";

        testValidator = new PscIdProvidedValidator(validation);
    }

    @AfterEach
    void tearDown() {
        pscVerificationData = null;
    }

    @Test
    void validateWhenPscIdProvided() {
        pscVerificationData = new PscVerificationData(
            "12345678",
            PSC_ID,
            verificationDetails
        );

        testValidator.validate(
            new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors, is(empty()));
    }

    @Test
    void validateWhenPscIdNotProvided() {
        var fieldError = new FieldError("object", "psc_notification_id", null, false,
            new String[]{null, "psc_notification_id"}, null,
            "not-provided default message");

        pscVerificationData = mock(PscVerificationData.class);

        when(pscVerificationData.pscNotificationId()).thenReturn(null);
        when(validation.get("psc-notification-id-missing")).thenReturn("not-provided default message");

        testValidator.validate(
            new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors.stream().findFirst().orElseThrow(), equalTo(fieldError));
        assertThat(errors, contains(fieldError));
    }
}