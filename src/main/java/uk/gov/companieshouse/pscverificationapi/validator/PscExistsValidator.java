package uk.gov.companieshouse.pscverificationapi.validator;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

/**
 * Validator for checking if a PSC entity exists.
 * <p>
 * Uses the {@link PscLookupService} to verify existence and adds errors if not found.
 * </p>
 */
@Component
public class PscExistsValidator extends BaseVerificationValidator implements VerificationValidator {

    private final PscLookupService pscLookupService;

    public PscExistsValidator(Map<String, String> validation, final PscLookupService pscLookupService) {
        super(validation);
        this.pscLookupService = pscLookupService;
    }

    /**
     * Checks if the PSC entity exists using the provided validation context.
     *
     * @param validationContext the validation context
     */
    @Override
    public void validate(final VerificationValidationContext validationContext) {

        try {
            pscLookupService.getPscIndividualFullRecord(validationContext.transaction(), validationContext.dto(), validationContext.pscType());
            super.validate(validationContext);
        }
        catch (FilingResourceNotFoundException e) {
            validationContext.errors().add(
                new FieldError("object", "psc_notification_id", validationContext.dto().pscNotificationId(), false,
                    new String[]{null, "notFound.psc_notification_id"}, null, validation.get("psc-notification-id-not-found")));
        }

    }
}
