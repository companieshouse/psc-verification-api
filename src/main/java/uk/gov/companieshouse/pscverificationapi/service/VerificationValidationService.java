package uk.gov.companieshouse.pscverificationapi.service;

import uk.gov.companieshouse.pscverificationapi.validator.VerificationValidationContext;


/**
 * The verification validation service layer that passes to the chain of validators
 * as defined in ValidatorConfig.
 */
public interface VerificationValidationService {

    /**
     * Apply the chain of validation steps appropriate for the given verification.
     *
     * @param context the verification filing data to be validated, with supporting context
     */
    void validate(VerificationValidationContext context);
}
