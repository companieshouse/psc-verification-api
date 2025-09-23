package uk.gov.companieshouse.pscverificationapi.validator;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

/**
 * Validator for checking if a PSC notification ID is provided in the request.
 */
@Component
public class PscIdProvidedValidator extends BaseVerificationValidator implements
    VerificationValidator {

    public PscIdProvidedValidator(final Map<String, String> validation) {
        super(validation);
    }

    /**
     * Validates if a PSC ID is provided in the request.
     *
     * @param validationContext the validation context
     */
    @Override
    public void validate(final VerificationValidationContext validationContext) {

        if (Optional.ofNullable(validationContext.dto().pscNotificationId()).isEmpty()) {
            validationContext.errors()
                .add(new FieldError("object", "psc_notification_id", null,
                    false, new String[]{null, "psc_notification_id"}, null,
                    validation.get("psc-notification-id-missing")));
        }else {
            super.validate(validationContext);
        }
    }

}
