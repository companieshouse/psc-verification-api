package uk.gov.companieshouse.pscverificationapi.validator;

import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.pscverificationapi.service.IdvLookupService;

@Component
public class UvidExistsValidator extends BaseVerificationValidator implements
    VerificationValidator {

    private final IdvLookupService idvLookupService;

    public UvidExistsValidator(final Map<String, String> validation, final IdvLookupService idvLookupService) {
        super(validation);
        this.idvLookupService = idvLookupService;
    }

    /**
     * Validates if the UVID exists.
     *
     * @param validationContext the validation context
     */
    @Override
    public void validate(final VerificationValidationContext validationContext) {

        //TODO

        super.validate(validationContext);
    }

}
