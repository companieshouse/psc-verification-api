package uk.gov.companieshouse.pscverificationapi.service.impl;

import static uk.gov.companieshouse.pscverificationapi.model.FilingKind.PSC_VERIFICATION_INDIVIDUAL;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.config.FilingDataConfig;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.helper.LogMapHelper;
import uk.gov.companieshouse.pscverificationapi.service.FilingDataService;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;
import uk.gov.companieshouse.pscverificationapi.utils.MapHelper;

/**
 * Produces Filing Data format for consumption as JSON by filing-resource-handler external service.
 * <p>
 * Implements {@link FilingDataService}
 * </p>
 */
@Service
public class FilingDataServiceImpl implements FilingDataService {

    private final PscVerificationService pscVerificationService;
    private final FilingDataConfig filingDataConfig;
    private final Logger logger;

    public FilingDataServiceImpl(final PscVerificationService pscVerificationService,
                                 final FilingDataConfig filingDataConfig, final Logger logger) {
        this.pscVerificationService = pscVerificationService;
        this.filingDataConfig = filingDataConfig;
        this.logger = logger;
    }

    @Override
    public FilingApi generateFilingApi(final String filingId, final Transaction transaction) {

        final var transactionId = transaction.getId();
        final var logMap = LogMapHelper.createLogMap(transactionId, filingId);
        logger.debugContext(transactionId, "Fetching PSC verification", logMap);

        final var pscVerificationOpt = pscVerificationService.get(filingId);
        final var pscVerification = pscVerificationOpt.orElseThrow(() -> new FilingResourceNotFoundException(
                String.format("PSC verification not found when generating filing for %s", filingId)));

        final var filingApi = new FilingApi();
        filingApi.setKind(PSC_VERIFICATION_INDIVIDUAL.getValue());
        filingApi.setDescription(filingDataConfig.getPscVerificationDescription());

        final var dataMap = MapHelper.convertObject(pscVerification.getData(), PropertyNamingStrategies.SNAKE_CASE);
        dataMap.remove("psc_notification_id");
        dataMap.put("appointment_id", pscVerification.getInternalData().internalId());
        logMap.put("Filing data to submit", dataMap);
        logger.debugContext(transactionId, filingId, logMap);
        filingApi.setData(dataMap);

        return filingApi;
    }

}