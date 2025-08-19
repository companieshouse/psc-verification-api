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
 * Validator to check if a PSC (Person with Significant Control) is unverified.
 * Determines unverified status based on identity verification details, such as
 * the absence of an appointment verification start date or a revoked verification.
 */
@Component
public class PscIsUnverifiedValidator extends BaseVerificationValidator implements VerificationValidator {
    private final PscLookupService pscLookupService;
    private final Logger logger;

    /**
     * Constructs a new {@code PscIsPastStartDateValidator}.
     *
     * @param validation a map of validation messages or configurations
     * @param pscLookupService the service used to retrieve PSC details
     * @param logger the logger for logging validation events
     */
    public PscIsUnverifiedValidator(Map<String, String> validation, PscLookupService pscLookupService, final Logger logger) {
        super(validation);
        this.pscLookupService = pscLookupService;
        this.logger = logger;
    }

    /**
     * Validates if the PSC is unverified based on the appointment verification statement start date or if the
     * appointmentVerificationEndOn date is in the past. If the start date is in the future, a validation error is added.
     * <p>
     * If the identity verification details are null, logs the event and skips validation.
     *
     * @param validationContext the context containing transaction, DTO, and PSC type information.
     */
    @Override
    public void validate(VerificationValidationContext validationContext) {
        PscIndividualFullRecordApi pscIndividualFullRecordApi = pscLookupService.getPscIndividualFullRecord(
                validationContext.transaction(), validationContext.dto(), validationContext.pscType());

        var identityVerificationDetails = pscIndividualFullRecordApi.getIdentityVerificationDetails();

        if (identityVerificationDetails == null) {
            logger.info(String.format(
                    "Validation for verification skipped due to null identity verification details. [Company number: %s, PSC notification ID: %s]",
                    validationContext.dto().companyNumber(), validationContext.dto().pscNotificationId()));

        } else {
            final var verificationDate = identityVerificationDetails.appointmentVerificationStartOn();

            //if the appointmentVerificationEndOn is in the future, then the PSC is already verified
            //revoked verifications will have an end date in the past
            if((identityVerificationDetails.appointmentVerificationEndOn() != null && identityVerificationDetails.appointmentVerificationEndOn().isAfter(LocalDate.now()))
                    //or if there is an active verification date
                    || (verificationDate != null && !LocalDate.now().isAfter(verificationDate))
            ) {
                // PSC is already verified
                    final var formattedVerificationDate = verificationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    final var errorResponseText = validation.get("psc-already-verified").replace("{appointment_verification_start_on}", formattedVerificationDate);

                    validationContext.errors().add(
                        new FieldError("object", "appointment_verification_start_on", formattedVerificationDate, false,
                                new String[]{null, formattedVerificationDate}, null, errorResponseText));
            }
        }

        super.validate(validationContext);
    }

}
