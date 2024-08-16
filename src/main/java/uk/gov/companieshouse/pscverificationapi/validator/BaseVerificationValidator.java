package uk.gov.companieshouse.pscverificationapi.validator;

import java.util.Map;
import java.util.Optional;

/**
 *  * Abstract base class for the verification service validators.
 *  * This class provides a mechanism to chain multiple validators together.
 */
public abstract class BaseVerificationValidator implements VerificationValidator {

    protected Map<String, String> validation;
    private VerificationValidator nextValidator;

    public BaseVerificationValidator(final Map<String, String> validation) {
        this.validation = validation;
    }

    @Override
    public void validate(final VerificationValidationContext validationContext) {
        Optional.ofNullable(nextValidator).ifPresent(v -> v.validate(validationContext));
    }

    @Override
    public void setNext(final VerificationValidator verificationValidator) {
        this.nextValidator = verificationValidator;
    }
}
