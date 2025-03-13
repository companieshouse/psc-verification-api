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
import uk.gov.companieshouse.api.model.psc.PscIndividualFullRecordApi;
import uk.gov.companieshouse.api.model.psc.VerificationState;
import uk.gov.companieshouse.api.model.psc.VerificationStatus;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
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

    PscIsUnverifiedValidator testValidator;
    private PscType pscType;
    private Set<FieldError> errors;
    private String passthroughHeader;

    @BeforeEach
    void setUp() {

        errors = new HashSet<>();
        pscType = PscType.INDIVIDUAL;
        passthroughHeader = "passthroughHeader";

        testValidator = new PscIsUnverifiedValidator(validation, pscLookupService);
        when(pscLookupService.getPscIndividualFullRecord(transaction, pscVerificationData, pscType))
                .thenReturn(pscIndividualFullRecord);
    }

    @Test
    void validateWhenPscHasNoVerificationState() {

        when(pscLookupService.getPscIndividualFullRecord(transaction, pscVerificationData, pscType))
                .thenReturn(pscIndividualFullRecord);
        testValidator.validate(
                new VerificationValidationContext(pscVerificationData, errors, transaction, pscType,
                        passthroughHeader));

        assertThat(errors, is(empty()));
    }

    @Test
    void validateWhenPscHasUnverifiedStatus() {

        var verificationState = new VerificationState(VerificationStatus.UNVERIFIED,
                LocalDate.now().minusDays(7), LocalDate.now().plusDays(7));
        when(pscIndividualFullRecord.getVerificationState()).thenReturn(verificationState);

        when(pscLookupService.getPscIndividualFullRecord(transaction, pscVerificationData, pscType))
                .thenReturn(pscIndividualFullRecord);
        testValidator.validate(
                new VerificationValidationContext(pscVerificationData, errors, transaction, pscType,
                        passthroughHeader));

        assertThat(errors, is(empty()));
    }

    @Test
    void validateWhenPscIsAlreadyVerified() {

        var verificationState = new VerificationState(VerificationStatus.VERIFIED,
                LocalDate.now().minusDays(7), LocalDate.now().plusDays(7));
        when(pscIndividualFullRecord.getVerificationState()).thenReturn(verificationState);

        var verificationStatus = pscIndividualFullRecord.getVerificationState().verificationStatus();
        var fieldError = new FieldError("object", "psc_verification_status", verificationStatus, false,
                new String[] { null, verificationStatus.toString() }, null,
                "This PSC has already provided their identity verification details");

        when(pscLookupService.getPscIndividualFullRecord(transaction, pscVerificationData, pscType))
                .thenReturn(pscIndividualFullRecord);
        when(validation.get("psc-already-verified"))
                .thenReturn("This PSC has already provided their identity verification details");

        testValidator.validate(
                new VerificationValidationContext(pscVerificationData, errors, transaction, pscType,
                        passthroughHeader));

        assertThat(errors.stream().findFirst().orElseThrow(), equalTo(fieldError));
        assertThat(errors, contains(fieldError));
    }
}
