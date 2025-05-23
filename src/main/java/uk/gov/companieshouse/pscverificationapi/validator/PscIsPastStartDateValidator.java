package uk.gov.companieshouse.pscverificationapi.validator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import uk.gov.companieshouse.api.model.psc.PscIndividualFullRecordApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

@Component
public class PscIsPastStartDateValidator extends BaseVerificationValidator implements VerificationValidator {
    private final PscLookupService pscLookupService;
    private final Logger logger;

    public PscIsPastStartDateValidator(Map<String, String> validation, PscLookupService pscLookupService, Logger logger) {
        super(validation);
        this.pscLookupService = pscLookupService;
        this.logger = logger;
    }

    @Override
    public void validate(VerificationValidationContext validationContext) {
        PscIndividualFullRecordApi pscIndividualFullRecordApi = pscLookupService.getPscIndividualFullRecord(
                validationContext.transaction(), validationContext.dto(), validationContext.pscType());

        final var verificationState = pscIndividualFullRecordApi.getVerificationState();

        if (verificationState == null) {
            logger.info(String.format(
                "Validation for PSC start date skipped due to null verification state. [Company number: %s, PSC notification ID: %s]",
                validationContext.dto().companyNumber(), validationContext.dto().pscNotificationId()));
        } else {
            final var startDate = verificationState.verificationStartDate();

            if (startDate != null && LocalDate.now().isBefore(startDate)) {
                final var formattedStartDate = startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                final var errorResponseText = validation.get("psc-cannot-verify-yet").replace("{start-date}", formattedStartDate);
                validationContext.errors().add(
                        new FieldError("object", "psc_verification_start_date", formattedStartDate, false,
                                new String[] { null, formattedStartDate }, null, errorResponseText));
            }
        }

        super.validate(validationContext);
    }

}
