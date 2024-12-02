package uk.gov.companieshouse.pscverificationapi.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.model.psc.PscApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

@ExtendWith(MockitoExtension.class)
class PscIsActiveValidatorTest {

    @Mock
    private PscLookupService pscLookupService;
    @Mock
    private PscVerificationData pscVerificationData;
    @Mock
    private Map<String, String> validation;
    @Mock
    private Transaction transaction;
    @Mock
    private PscApi pscApi;
    @Mock
    private ApiErrorResponseException errorResponseException;

    PscIsActiveValidator testValidator;
    private PscType pscType;
    private Set<FieldError> errors;
    private String passthroughHeader;

    private static final String PSC_ID = "67edfE436y35hetsie6zuAZtr";
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 5, 5);

    @BeforeEach
    void setUp() {

        errors = new HashSet<>();
        pscType = PscType.INDIVIDUAL;
        passthroughHeader = "passthroughHeader";

        testValidator = new PscIsActiveValidator(validation, pscLookupService);
        when(pscLookupService.getPsc(transaction, pscVerificationData, pscType, passthroughHeader)).thenReturn(pscApi);
    }

    @Test
    void validateWhenPscIsActive() {

        when(pscLookupService.getPsc(transaction, pscVerificationData, pscType,
            passthroughHeader)).thenReturn(pscApi);
        testValidator.validate(
            new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors, is(empty()));
    }

    @Test
    void validateWhenPscIsCeased() {

        when(pscVerificationData.pscAppointmentId()).thenReturn(PSC_ID);
        when(pscApi.getCeasedOn()).thenReturn(TEST_DATE);

        var fieldError = new FieldError("object", "psc_appointment_id", pscVerificationData.pscAppointmentId(), false,
            new String[]{null, PSC_ID}, null, "is ceased default message");

        when(pscLookupService.getPsc(transaction, pscVerificationData, pscType,
            passthroughHeader)).thenReturn(pscApi);
        when(validation.get("psc-is-ceased")).thenReturn("is ceased default message");

        testValidator.validate(
            new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors.stream().findFirst().orElseThrow(), equalTo(fieldError));
        assertThat(errors, contains(fieldError));
    }
}