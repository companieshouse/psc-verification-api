package uk.gov.companieshouse.pscverificationapi.controller.impl;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.controller.ValidationStatusController;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.helper.LogMapHelper;
import uk.gov.companieshouse.pscverificationapi.mapper.ErrorMapper;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;
import uk.gov.companieshouse.pscverificationapi.service.VerificationValidationService;
import uk.gov.companieshouse.pscverificationapi.validator.VerificationValidationContext;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

/**
 * Implementation of the {@link ValidationStatusController} interface.
 */
@RestController
@RequestMapping("/transactions/{transactionId}/persons-with-significant-control-verification")
public class ValidationStatusControllerImpl implements ValidationStatusController {
    private final PscVerificationService pscVerificationService;
    private final VerificationValidationService validatorService;
    private final ErrorMapper errorMapper;
    private final Logger logger;

    public ValidationStatusControllerImpl(final PscVerificationService pscVerificationService,
                                          VerificationValidationService validatorService, final ErrorMapper errorMapper,
                                          final Logger logger) {
        this.pscVerificationService = pscVerificationService;
        this.validatorService = validatorService;
        this.errorMapper = errorMapper;
        this.logger = logger;

    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{filingResourceId}/validation_status", produces = {"application/json"})
    public ValidationStatusResponse validate(@PathVariable("transactionId") final String transId,
                                             @PathVariable("filingResourceId") final String filingResource,
                                             @RequestAttribute(required = false, name = "transaction")
                                             final Transaction transaction, final HttpServletRequest request) {

        final var logMap = LogMapHelper.createLogMap(transId, filingResource);
        logMap.put("path", request.getRequestURI());
        logMap.put("method", request.getMethod());
        logger.debugRequest(request, "GET validation request", logMap);

        final var passthroughHeader = request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());
        final var pscVerification = pscVerificationService.get(filingResource);

        return pscVerification.map(f -> isValid(f, passthroughHeader, transaction))
                .orElseThrow(() -> new FilingResourceNotFoundException(filingResource));
    }

    /**
     * Checks if the PSC verification is valid and returns the validation status response.
     *
     * @param pscVerification the PSC verification entity
     * @param passthroughHeader the passthrough header
     * @param transaction the transaction
     * @return ValidationStatusResponse object
     */
    private ValidationStatusResponse isValid(final PscVerification pscVerification, final String passthroughHeader,
                                             final Transaction transaction) {

        final var validationStatus = new ValidationStatusResponse();
            final var validationErrors = calculateIsValid(pscVerification, passthroughHeader, transaction);

            validationStatus.setValid(validationErrors.length == 0);
            validationStatus.setValidationStatusError(validationErrors);
        return validationStatus;
    }

    /**
     * Calculates validation errors for the PSC verification.
     *
     * @param pscVerification the PSC verification entity
     * @param passthroughHeader the passthrough header
     * @param transaction the transaction
     * @return array of ValidationStatusError
     */
    private ValidationStatusError[] calculateIsValid(final PscVerification pscVerification,
                                                     final String passthroughHeader, final Transaction transaction) {

        final var errors = new HashSet<FieldError>();

        VerificationValidationContext context = new VerificationValidationContext(
            pscVerification.getData(), errors, transaction, PscType.INDIVIDUAL, passthroughHeader);

        validatorService.validate(context);
        return errorMapper.map(context.errors());
    }
}