package uk.gov.companieshouse.pscverificationapi.validator;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import uk.gov.companieshouse.api.model.psc.PscIndividualFullRecordApi;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

@Component
public class PscIsPastStartDateValidator extends BaseVerificationValidator implements VerificationValidator {
    private final PscLookupService pscLookupService;

    public PscIsPastStartDateValidator(Map<String, String> validation, PscLookupService pscLookupService) {
        super(validation);
        this.pscLookupService = pscLookupService;
    }

    @Override
    public void validate(VerificationValidationContext validationContext) {
        PscIndividualFullRecordApi pscIndividualFullRecordApi = pscLookupService.getPscIndividualFullRecord(
                validationContext.transaction(), validationContext.dto(), validationContext.pscType());

        var startDate = pscIndividualFullRecordApi.getVerificationState().verificationStartDate();
        if (startDate != null && LocalDate.now().isBefore(startDate)) {
            validationContext.errors().add(
                    new FieldError("object", "psc_verification_start_date", startDate, false,
                            new String[] { null, startDate.toString() }, null,
                            validation.get("psc-cannot-verify-yet")));
        }

        super.validate(validationContext);
    }

}
