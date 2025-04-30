package uk.gov.companieshouse.pscverificationapi.validator;

import static uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants.INDIVIDUAL_VERIFIED;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

@Component
public class PscVerificationStatementPresentValidator extends BaseVerificationValidator implements
    VerificationValidator {

    public PscVerificationStatementPresentValidator(final Map<String, String> validation) {
        super(validation);
    }

    /**
     * Validates if the PSC verification statement is present.
     *
     * @param validationContext the validation context
     */
    @Override
    public void validate(final VerificationValidationContext validationContext) {

        if (!validationContext.dto().verificationDetails().statements().contains(INDIVIDUAL_VERIFIED)) {
            validationContext.errors().add(
                new FieldError("object", "verification_statement", validationContext.dto().verificationDetails().statements(), false,
                    new String[]{null, validationContext.dto().verificationDetails().statements().toString()}, null, validation.get("verification-details-not-provided")));
        }

        super.validate(validationContext);
    }

}
