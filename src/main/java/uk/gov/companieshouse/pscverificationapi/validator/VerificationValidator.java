package uk.gov.companieshouse.pscverificationapi.validator;

public interface VerificationValidator {

    boolean validate(final VerificationValidationContext validationContext);

    void setNext(final VerificationValidator verificationValidator);
}
