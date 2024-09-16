package uk.gov.companieshouse.pscverificationapi.validator;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

@Component
public class PscExistsValidator extends BaseVerificationValidator implements VerificationValidator {

    private final PscLookupService pscLookupService;

    public PscExistsValidator(Map<String, String> validation, final PscLookupService pscLookupService) {
        super(validation);
        this.pscLookupService = pscLookupService;
    }

    /**
     * Validates that the PSC entity is in an active state.
     *
     * @param validationContext the validation context
     * @return boolean whether the validation passes
     */
    @Override
    public boolean validate(final VerificationValidationContext validationContext) {
        setValid(false);

        try {
            pscLookupService.getPsc(validationContext.transaction(), validationContext.dto().pscAppointmentId(), validationContext.pscType(),
                validationContext.passthroughHeader());
            setValid(true);
        }
        catch (FilingResourceNotFoundException e) {
            validationContext.errors().add(
                new FieldError("object", "psc_appointment_id", validationContext.dto().pscAppointmentId(), false,
                    new String[]{null, validationContext.dto().pscAppointmentId()}, null, validation.get("psc_appointment_id-not-found")));

        }

        super.validate(validationContext);
        return isValid();
    }

}
