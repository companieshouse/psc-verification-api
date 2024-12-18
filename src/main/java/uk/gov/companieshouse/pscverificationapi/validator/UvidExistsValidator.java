package uk.gov.companieshouse.pscverificationapi.validator;

import static uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse.AccuracyStatementEnum.FORENAMES_MISMATCH;
import static uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse.AccuracyStatementEnum.SURNAME_MISMATCH;

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
import java.util.Objects;

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
            dto, PscType.INDIVIDUAL);

        UvidMatchResponse uvidMatchResponse;
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
            // Determine the action based on the accuracy statement
            if (Objects.requireNonNull(accuracyStatement) == FORENAMES_MISMATCH || accuracyStatement == SURNAME_MISMATCH) {
                if (dto.verificationDetails().nameMismatchReason() != null) {
                    continue;
                }
                validationContext.errors().add(
                    new FieldError("object", "uvid_match", dto.verificationDetails().uvid(), false,
                        new String[]{null, dto.verificationDetails().uvid()}, null,
                        validation.get("no-name-mismatch-reason")));
            } else {
                validationContext.errors().add(
                    new FieldError("object", "uvid_match", dto.verificationDetails().uvid(), false,
                        new String[]{null, dto.verificationDetails().uvid()}, null,
                        validation.get(snakeToKebab(accuracyStatement.getValue()))));
            }
        }
    }

    private String snakeToKebab(String str) {

        return str.replace("_", "-");
    }

}
