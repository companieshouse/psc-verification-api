package uk.gov.companieshouse.pscverificationapi.validator;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import uk.gov.companieshouse.api.model.psc.PscIndividualFullRecordApi;
import uk.gov.companieshouse.api.model.psc.VerificationStatus;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

@Component
public class PscIsUnverifiedValidator extends BaseVerificationValidator implements VerificationValidator {
    private final PscLookupService pscLookupService;

    public PscIsUnverifiedValidator(Map<String, String> validation, PscLookupService pscLookupService) {
        super(validation);
        this.pscLookupService = pscLookupService;
    }

    @Override
    public void validate(VerificationValidationContext validationContext) {
        PscIndividualFullRecordApi pscIndividualFullRecordApi = pscLookupService.getPscIndividualFullRecord(
                validationContext.transaction(), validationContext.dto(), validationContext.pscType());

        var verificationState = pscIndividualFullRecordApi.getVerificationState();
        if (verificationState != null && verificationState.verificationStatus() == VerificationStatus.VERIFIED) {
            validationContext.errors().add(
                    new FieldError("object", "psc_verification_status", verificationState.verificationStatus(), false,
                            new String[] { null, verificationState.verificationStatus().toString() }, null,
                            validation.get("psc-already-verified")));
        }
    }

}
