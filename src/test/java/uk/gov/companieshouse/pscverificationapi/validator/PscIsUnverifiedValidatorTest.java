package uk.gov.companieshouse.pscverificationapi.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.psc.IdentityVerificationDetails;
import uk.gov.companieshouse.api.model.psc.PscIndividualFullRecordApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

@ExtendWith(MockitoExtension.class)
class PscIsUnverifiedValidatorTest {

    @Mock
    private PscLookupService pscLookupService;
    @Mock
    private PscVerificationData pscVerificationData;
    @Mock
    private Map<String, String> validation;
    @Mock
    private Transaction transaction;
    @Mock
    private PscIndividualFullRecordApi pscIndividualFullRecord;
    @Mock
    private Logger logger;

    PscIsUnverifiedValidator testValidator;
    private PscType pscType;
    private Set<FieldError> errors;
    private String passthroughHeader;

    @BeforeEach
    void setUp() {

        errors = new HashSet<>();
        pscType = PscType.INDIVIDUAL;
        passthroughHeader = "passthroughHeader";

        testValidator = new PscIsUnverifiedValidator(validation, pscLookupService, logger);
        when(pscLookupService.getPscIndividualFullRecord(transaction, pscVerificationData, pscType))
                .thenReturn(pscIndividualFullRecord);
    }

    @Test
    void validateWhenPscHasNoIdentityVerificationDetails() {

        when(pscLookupService.getPscIndividualFullRecord(transaction, pscVerificationData, pscType))
                .thenReturn(pscIndividualFullRecord);

        testValidator.validate(
                new VerificationValidationContext(pscVerificationData, errors, transaction, pscType,
                        passthroughHeader));

        assertThat(errors, is(empty()));
    }

    @Test
    void validateWhenPscIsUnverified() {
        var identityVerificationDetails = new IdentityVerificationDetails(null, null, LocalDate.now().minusDays(7), LocalDate.now().plusDays(7));

        when(pscIndividualFullRecord.getIdentityVerificationDetails()).thenReturn(identityVerificationDetails);

        when(pscLookupService.getPscIndividualFullRecord(transaction, pscVerificationData, pscType))
                .thenReturn(pscIndividualFullRecord);
        testValidator.validate(
                new VerificationValidationContext(pscVerificationData, errors, transaction, pscType,
                        passthroughHeader));

        assertThat(errors, is(empty()));
    }

    @Test
    void validateWhenPscIsVerified() {
        var startDate = LocalDate.now();
        var formattedVerificationDate = startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        var identityVerificationDetails = new IdentityVerificationDetails(LocalDate.now(), LocalDate.now().plusDays(20000), LocalDate.now().minusDays(7), LocalDate.now().plusDays(7));

        when(pscIndividualFullRecord.getIdentityVerificationDetails()).thenReturn(identityVerificationDetails);
        when(pscLookupService.getPscIndividualFullRecord(transaction, pscVerificationData, pscType))
                .thenReturn(pscIndividualFullRecord);
        when(validation.get("psc-already-verified")).thenReturn("This PSC has already provided their identity verification details");

        var errorResponseText = validation.get("psc-already-verified").replace("{appointment_verification_start_on}", formattedVerificationDate);
        var fieldError = new FieldError("object", "appointment_verification_start_on", formattedVerificationDate, false,
                new String[] { null, formattedVerificationDate }, null, errorResponseText);

        testValidator.validate(
                new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors.stream().findFirst().orElseThrow(), equalTo(fieldError));

        assertThat(errors, contains(fieldError));
    }

    @Test
    void validateWhenPscVerificationExpiresToday() {
        var identityVerificationDetails = new IdentityVerificationDetails(LocalDate.now().minusDays(365), LocalDate.now(), LocalDate.now().minusDays(365), LocalDate.now().minusDays(351));

        when(pscIndividualFullRecord.getIdentityVerificationDetails()).thenReturn(identityVerificationDetails);
        when(pscLookupService.getPscIndividualFullRecord(transaction, pscVerificationData, pscType))
                .thenReturn(pscIndividualFullRecord);

        testValidator.validate(
                new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors, is(empty()));
    }

    @Test
    void validateWhenPscVerificationHasExpired() {
        var identityVerificationDetails = new IdentityVerificationDetails(LocalDate.now().minusDays(365), LocalDate.now().minusDays(1), LocalDate.now().minusDays(365), LocalDate.now().minusDays(351));

        when(pscIndividualFullRecord.getIdentityVerificationDetails()).thenReturn(identityVerificationDetails);
        when(pscLookupService.getPscIndividualFullRecord(transaction, pscVerificationData, pscType))
                .thenReturn(pscIndividualFullRecord);

        testValidator.validate(
                new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors, is(empty()));
    }

}
