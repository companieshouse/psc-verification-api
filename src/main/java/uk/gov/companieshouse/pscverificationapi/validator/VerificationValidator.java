package uk.gov.companieshouse.pscverificationapi.validator;

public interface VerificationValidator {

    void validate(final VerificationValidationContext validationContext);

    void setNext(final VerificationValidator verificationValidator);
}
