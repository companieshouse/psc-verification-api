package uk.gov.companieshouse.pscverificationapi.validator;

/**
 * Interface for PSC verification validators in a validation chain.
 * <p>
 * Defines methods for validation and chaining of validators.
 * </p>
 */
public interface VerificationValidator {

    void validate(final VerificationValidationContext validationContext);

    void setNext(final VerificationValidator verificationValidator);
}
