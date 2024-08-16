package uk.gov.companieshouse.pscverificationapi.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.FieldError;
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

    PscIsActiveValidator testValidator;
    private PscType pscType;
    private List<FieldError> errors;
    private String passthroughHeader;

    private static final String PSC_ID = "67edfE436y35hetsie6zuAZtr";
    private static final LocalDate CEASED_ON = LocalDate.of(2024,1,21);

    @BeforeEach
    void setUp() {

        errors = new ArrayList<>();
        pscType = PscType.INDIVIDUAL;
        passthroughHeader = "passthroughHeader";

        testValidator = new PscIsActiveValidator(validation, pscLookupService);
        when(pscVerificationData.pscAppointmentId()).thenReturn(PSC_ID);
        when(pscLookupService.getPsc(transaction, PSC_ID, pscType, passthroughHeader)).thenReturn(pscApi);
    }


    @Test
    void validateWhenPscIsActive() {

        when(pscApi.getCeasedOn()).thenReturn(null);
        testValidator.validate(
            new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors, is(empty()));
    }

    @Test
    void validateWhenPscIsCeased() {

        var fieldError = new FieldError("object", "psc_appointment_id", CEASED_ON, false,
            new String[]{null, "date.ceased_on"}, null, "is ceased default message");

        when(pscApi.getCeasedOn()).thenReturn(CEASED_ON);
        when(validation.get("psc-is-ceased")).thenReturn("is ceased default message");

        testValidator.validate(
            new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors.stream().findFirst().orElseThrow(), equalTo(fieldError));
        assertThat(errors, contains(fieldError));
    }
}