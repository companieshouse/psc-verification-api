package uk.gov.companieshouse.pscverificationapi.validator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import uk.gov.companieshouse.api.model.psc.PscIndividualFullRecordApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

/**
 * Validator to check if a Person with Significant Control (PSC) can be verified
 * based on the appointment verification statement start date.
 * <p>
 * If the start date is in the future, an error is added to the validation context.
 * </p>
 */
@Component
public class PscIsPastStartDateValidator extends BaseVerificationValidator implements VerificationValidator {
    private final PscLookupService pscLookupService;
    private final Logger logger;

    /**
     * Constructs a new {@code PscIsPastStartDateValidator}.
     *
     * @param validation a map of validation messages or configurations
     * @param pscLookupService the service used to retrieve PSC details
     * @param logger the logger for logging validation events
     */
    public PscIsPastStartDateValidator(Map<String, String> validation, PscLookupService pscLookupService, Logger logger) {
        super(validation);
        this.pscLookupService = pscLookupService;
        this.logger = logger;
    }

    /**
     * Validates whether the PSC can be verified based on the appointment verification statement start date.
     * <p>
     * If the identity verification details are null, logs the event and skips validation.
     * If the start date is in the future, adds a validation error.
     * </p>
     */
    @Override
    public void validate(VerificationValidationContext validationContext) {
        PscIndividualFullRecordApi pscIndividualFullRecordApi = pscLookupService.getPscIndividualFullRecord(
                validationContext.transaction(), validationContext.dto(), validationContext.pscType());

        final var identityVerificationDetails = pscIndividualFullRecordApi.getIdentityVerificationDetails();

        if (identityVerificationDetails == null) {
            logger.info(String.format(
                "Validation for PSC start date skipped due to null identity verification details. [Company number: %s, PSC notification ID: %s]",
                validationContext.dto().companyNumber(), validationContext.dto().pscNotificationId()));
        } else {
            final var startDate = identityVerificationDetails.appointmentVerificationStatementDate();

            if (startDate != null && startDate.isAfter(LocalDate.now())) {
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
