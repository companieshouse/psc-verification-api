package uk.gov.companieshouse.pscverificationapi.validator;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceInvalidException;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

@Component
public class PscIsActiveValidator extends BaseVerificationValidator implements
    VerificationValidator {

    private final PscLookupService pscLookupService;

    public PscIsActiveValidator(final Map<String, String> validation, final PscLookupService pscLookupService) {
        super(validation);
        this.pscLookupService = pscLookupService;
    }

    /**
     * Validates if the PSC is in an active state.
     *
     * @param validationContext the validation context
     * @return boolean if validation passes
     */
    @Override
    public boolean validate(final VerificationValidationContext validationContext) {
        setValid(false);

        try {
            pscLookupService.getPsc(validationContext.transaction(), validationContext.dto().pscAppointmentId(), validationContext.pscType(),
                validationContext.passthroughHeader());
            setValid(true);

        } catch (FilingResourceInvalidException e) {
            validationContext.errors().add(
                new FieldError("object", "psc_appointment_id", validationContext.dto().pscAppointmentId(), false,
                    new String[]{null, validationContext.dto().pscAppointmentId()}, null, validation.get("psc-is-ceased")));
        }

        super.validate(validationContext);
        return isValid();
    }

}
