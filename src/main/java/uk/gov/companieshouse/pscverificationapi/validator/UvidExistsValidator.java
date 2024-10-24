package uk.gov.companieshouse.pscverificationapi.validator;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.identityverification.model.UvidMatch;
import uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.exception.IdvLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.service.IdvLookupService;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

@Component
public class UvidExistsValidator extends BaseVerificationValidator implements
    VerificationValidator {

    public static final String DETAILS_MATCH_UVID = "details_match_uvid";

    private final IdvLookupService idvLookupService;
    private final PscLookupService pscLookupService;

    public UvidExistsValidator(final Map<String, String> validation, final IdvLookupService idvLookupService, PscLookupService pscLookupService) {
        super(validation);
        this.idvLookupService = idvLookupService;
        this.pscLookupService = pscLookupService;
    }

    /**
     * Validates if the UVID exists.
     *
     * @param validationContext the validation context
     */
    @Override
    public void validate(final VerificationValidationContext validationContext) {

        PscVerificationData dto = validationContext.dto();
        UvidMatch uvidMatch = pscLookupService.getUvidMatchWithPscData(validationContext.transaction(),
            dto, PscType.INDIVIDUAL, validationContext.passthroughHeader());

        UvidMatchResponse uvidMatchResponse = null;
        try {
            uvidMatchResponse = idvLookupService.matchUvid(uvidMatch);
        } catch (ApiErrorResponseException e) {
            throw new IdvLookupServiceException(MessageFormat.format("Error matching UVID {0}: {1} {2}",
                dto.verificationDetails().uvid(), null, e.getStatusMessage()), e);
        }

        List<UvidMatchResponse.AccuracyStatementEnum> accuracyStatementList = uvidMatchResponse.getAccuracyStatement();
        for (UvidMatchResponse.AccuracyStatementEnum accuracyStatement : accuracyStatementList) {

            if (accuracyStatement.getValue().equals(DETAILS_MATCH_UVID)) {
                break;
            }
            validationContext.errors().add(
                new FieldError("object", "uvid_match", dto.verificationDetails().uvid(), false,
                    new String[]{null, dto.verificationDetails().uvid()}, null,
                    validation.get(snakeToKebab(accuracyStatement.getValue()))));
        }

        super.validate(validationContext);
    }

    private String snakeToKebab(String str) {

        return str.replace("_", "-");
    }

}
