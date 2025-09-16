package uk.gov.companieshouse.pscverificationapi.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.psc.IdentityVerificationDetails;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.psc.IndividualFullRecord;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PscIsPastStartDateValidatorTest {

    @Mock
    private PscLookupService pscLookupService;
    @Mock
    private PscVerificationData pscVerificationData;
    @Mock
    private Map<String, String> validation;
    @Mock
    private Transaction transaction;
    @Mock
    private IndividualFullRecord individualFullRecord;
    @Mock
    private Logger logger;

    PscIsPastStartDateValidator testValidator;
    private PscType pscType;
    private Set<FieldError> errors;
    private String passthroughHeader;

    @BeforeEach
    void setUp() {

        errors = new HashSet<>();
        pscType = PscType.INDIVIDUAL;
        passthroughHeader = "passthroughHeader";

        testValidator = new PscIsPastStartDateValidator(validation, pscLookupService, logger);
        when(pscLookupService.getIndividualFullRecord(transaction, pscVerificationData, pscType))
                .thenReturn(individualFullRecord);
    }

    @Test
    void validateWhenIdentityVerificationDetailsIsNull() {
        when(individualFullRecord.getIdentityVerificationDetails()).thenReturn(null);
        when(pscLookupService.getIndividualFullRecord(transaction, pscVerificationData, pscType))
                .thenReturn(individualFullRecord);

        testValidator.validate(new VerificationValidationContext(pscVerificationData, errors, transaction, pscType,
                passthroughHeader));

        assertThat(errors, is(empty()));
    }

    @Test
    void validateWhenAppointmentVerificationStatementDateIsNull() {
        var identityVerificationDetails = new IdentityVerificationDetails();

        when(individualFullRecord.getIdentityVerificationDetails()).thenReturn(identityVerificationDetails);
        when(pscLookupService.getIndividualFullRecord(transaction, pscVerificationData, pscType))
                .thenReturn(individualFullRecord);

        testValidator.validate(new VerificationValidationContext(pscVerificationData, errors, transaction, pscType,
                        passthroughHeader));

        assertThat(errors, is(empty()));
    }

    @Test
    void validateWhenAppointmentVerificationStartOnIsToday() {
        var identityVerificationDetails = new IdentityVerificationDetails()
                .appointmentVerificationStatementDate(LocalDate.now())
                .appointmentVerificationStatementDueOn(LocalDate.now().plusDays(14));

        when(individualFullRecord.getIdentityVerificationDetails()).thenReturn(identityVerificationDetails);
        when(pscLookupService.getIndividualFullRecord(transaction, pscVerificationData, pscType))
                .thenReturn(individualFullRecord);

        testValidator.validate( new VerificationValidationContext(pscVerificationData, errors, transaction, pscType,
                passthroughHeader));

        assertThat(errors, is(empty()));
    }

    @Test
    void validateWhenAppointmentVerificationStartOnIsTomorrow() {
        var startDate = LocalDate.now().plusDays(1);
        var formattedStartDate = startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        var identityVerificationDetails = new IdentityVerificationDetails()
                .appointmentVerificationStatementDate(startDate)
                .appointmentVerificationStatementDueOn(LocalDate.now().plusDays(15));

        when(individualFullRecord.getIdentityVerificationDetails()).thenReturn(identityVerificationDetails);
        when(validation.get("psc-cannot-verify-yet"))
                .thenReturn("This PSC cannot provide their identity verification details until {start-date}. " +
                        "They must provide their details within 14 days of this date");

        var errorResponseText = validation.get("psc-cannot-verify-yet").replace("{start-date}", formattedStartDate);

        var fieldError = new FieldError("object", "psc_verification_start_date", formattedStartDate, 
                false, new String[] { null, formattedStartDate }, null, errorResponseText);

        when(pscLookupService.getIndividualFullRecord(transaction, pscVerificationData, pscType))
                .thenReturn(individualFullRecord);

        testValidator.validate(
                new VerificationValidationContext(pscVerificationData, errors, transaction, pscType,
                        passthroughHeader));

        assertThat(errors.stream().findFirst().orElseThrow(), equalTo(fieldError));
        assertThat(errors, contains(fieldError));
    }
}
