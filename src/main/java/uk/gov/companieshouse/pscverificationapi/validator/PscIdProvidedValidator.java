package uk.gov.companieshouse.pscverificationapi.validator;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

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

        if (Optional.ofNullable(validationContext.dto().pscAppointmentId()).isEmpty()) {
            validationContext.errors()
                .add(new FieldError("object", "psc_appointment_id", null,
                    false, new String[]{null, "psc_appointment_id"}, null,
                    validation.get("psc-appointment-id-missing")));
        }else {
            super.validate(validationContext);
        }
    }

}
