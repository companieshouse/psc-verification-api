package uk.gov.companieshouse.pscverificationapi.validator;

import static uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse.AccuracyStatementEnum.FORENAMES_MISMATCH;
import static uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse.AccuracyStatementEnum.SURNAME_MISMATCH;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.identityverification.model.UvidMatch;
import uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse;
import uk.gov.companieshouse.api.model.psc.IndividualFullRecord;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.psc.DateOfBirth;
import uk.gov.companieshouse.api.psc.NameElements;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.exception.IdvLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.exception.PscLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.helper.JsonHelper;
import uk.gov.companieshouse.pscverificationapi.service.IdvLookupService;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;

@Component
public class UvidExistsValidator extends BaseVerificationValidator implements
    VerificationValidator {

    private static final List<UvidMatchResponse.AccuracyStatementEnum> VALID_UVID_ACCURACY_STATEMENT =
            Collections.singletonList(UvidMatchResponse.AccuracyStatementEnum.DETAILS_MATCH_UVID);

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
        UvidMatch uvidMatch = getUvidMatchWithPscData(validationContext.transaction(),
            dto, PscType.INDIVIDUAL);

        UvidMatchResponse uvidMatchResponse;
        try {
            uvidMatchResponse = idvLookupService.matchUvid(uvidMatch);
        } catch (ApiErrorResponseException e) {
            throw new IdvLookupServiceException(MessageFormat.format("Error matching UVID {0}: {1} {2}",
                dto.verificationDetails().uvid(), null, e.getStatusMessage()), e);
        }

        List<UvidMatchResponse.AccuracyStatementEnum> accuracyStatementList = uvidMatchResponse.getAccuracyStatement();

        // Filter for when the uvid is valid but expired
        if (!accuracyStatementList.equals(VALID_UVID_ACCURACY_STATEMENT)) {
            checkForValidationErrors(accuracyStatementList.stream().filter(item ->
                            !item.equals(UvidMatchResponse.AccuracyStatementEnum.DETAILS_MATCH_UVID)).toList(), dto, validationContext);
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
                        new String[]{null, "no-name-mismatch-reason"}, null,
                        validation.get("no-name-mismatch-reason")));
            } else {
                validationContext.errors().add(
                    new FieldError("object", "uvid_match", dto.verificationDetails().uvid(), false,
                        new String[]{null, snakeToKebab(accuracyStatement.getValue())}, null,
                        validation.get(snakeToKebab(accuracyStatement.getValue()))));
            }
        }
    }

    public UvidMatch getUvidMatchWithPscData(final Transaction transaction,
                                             final PscVerificationData data, final PscType pscType)
        throws PscLookupServiceException {

        UvidMatch uvidMatch = new UvidMatch();
        Optional<String> uvid = Optional.ofNullable(data.verificationDetails().uvid());
        uvidMatch.setUvid(uvid.orElse(""));

        IndividualFullRecord pscIndividualFullRecord = pscLookupService.getPscIndividualFullRecord(transaction, data, pscType);
        setUvidDataFromPsc(uvidMatch, pscIndividualFullRecord);

        return uvidMatch;
    }

    private void setUvidDataFromPsc(UvidMatch uvidMatch, IndividualFullRecord pscData) {

        NameElements convertedNameElements = JsonHelper.convertLinkedHashmap(pscData.getNameElements(), PropertyNamingStrategies.SNAKE_CASE, NameElements.class);
        List<String> forenames = new ArrayList<>();

        String forename = convertedNameElements.getForename();
        String middleNames = convertedNameElements.getMiddleName();
        String surname = convertedNameElements.getSurname();

        if (forename != null) {
            forenames.add(forename);
        }
        if (middleNames != null) {
            forenames.add(middleNames);
        }
        uvidMatch.setForenames(forenames);
        uvidMatch.setSurname(surname);

        DateOfBirth dateOfBirth = JsonHelper.convertLinkedHashmap(pscData.getDateOfBirth(), PropertyNamingStrategies.SNAKE_CASE, DateOfBirth.class);
        String stringDateOfBirth = String.format("%04d-%02d-%02d", dateOfBirth.getYear(), dateOfBirth.getMonth(), dateOfBirth.getDay());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        uvidMatch.setDateOfBirth(LocalDate.parse(stringDateOfBirth, formatter));
    }

    private String snakeToKebab(String str) {

        return str.replace("_", "-");
    }

}
