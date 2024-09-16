package uk.gov.companieshouse.pscverificationapi.validator;

import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *  * Abstract base class for the verification service validators.
 *  * This class provides a mechanism to chain multiple validators together.
 */
public abstract class BaseVerificationValidator implements VerificationValidator {

    protected Map<String, String> validation;
    private VerificationValidator nextValidator;
    private boolean isValid;

    public BaseVerificationValidator(@Qualifier(value = "validation") final Map<String, String> validation) {
        this.validation = validation;
    }

    @Override
    public boolean validate(final VerificationValidationContext validationContext) {
        Optional.ofNullable(nextValidator).ifPresent(v -> v.validate(validationContext));
        return isValid();
    }

    @Override
    public void setNext(final VerificationValidator verificationValidator) {
        this.nextValidator = verificationValidator;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(final boolean valid) {
        isValid = valid;
    }
}
