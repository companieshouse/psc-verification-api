package uk.gov.companieshouse.pscverificationapi.validator;

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

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

@Component
public class UvidExistsValidator extends BaseVerificationValidator implements
    VerificationValidator {

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

        if (!accuracyStatementList.contains(UvidMatchResponse.AccuracyStatementEnum.DETAILS_MATCH_UVID)) {
            checkForValidationErrors(accuracyStatementList, dto, validationContext);
        }

        super.validate(validationContext);
    }

    public void checkForValidationErrors(List<UvidMatchResponse.AccuracyStatementEnum> accuracyStatementList,
                                        PscVerificationData dto, VerificationValidationContext validationContext) {

        for (UvidMatchResponse.AccuracyStatementEnum accuracyStatement : accuracyStatementList) {
            String accuracyStatementValue = switch (accuracyStatement.getValue()){
                case "forenames_mismatch", "surname_mismatch" -> "no-name-mismatch-reason";
                default -> accuracyStatement.getValue();
            };
            validationContext.errors().add(
                new FieldError("object", "uvid_match", dto.verificationDetails().uvid(), false,
                    new String[]{null, dto.verificationDetails().uvid()}, null,
                    validation.get(snakeToKebab(accuracyStatementValue))));
        }
    }

    private String snakeToKebab(String str) {

        return str.replace("_", "-");
    }

}
