package uk.gov.companieshouse.pscverificationapi.validator;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.psc.IndividualFullRecord;
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
     */
    @Override
    public void validate(final VerificationValidationContext validationContext) {

        IndividualFullRecord pscIndividualFullRecord = pscLookupService.getPscIndividualFullRecord(validationContext.transaction(), validationContext.dto(), validationContext.pscType());

        if (Optional.ofNullable(pscIndividualFullRecord.getCeasedOn()).isPresent()) {
            validationContext.errors().add(
                new FieldError("object", "psc_appointment_id", validationContext.dto().pscAppointmentId(), false,
                    new String[]{null, validationContext.dto().pscAppointmentId()}, null, validation.get("psc-is-ceased")));
        }

        super.validate(validationContext);
    }

}
