package uk.gov.companieshouse.pscverificationapi.validator;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.psc.PscApi;
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
     * @param validationContext the validation context
     */
    @Override
    public void validate(final VerificationValidationContext validationContext) {

        final PscApi psc;
        psc = pscLookupService.getPsc(validationContext.transaction(), validationContext.dto()
                    .pscAppointmentId(), validationContext.pscType(),
                validationContext.passthroughHeader());

        if (Optional.ofNullable(psc.getCeasedOn()).isPresent()) {
            validationContext.errors()
                .add(new FieldError("object", "psc_appointment_id", psc.getCeasedOn(),
                    false, new String[]{null, "date.ceased_on"}, null,
                    validation.get("psc-is-ceased")));
        }

        super.validate(validationContext);
    }

}
