package uk.gov.companieshouse.pscverificationapi.service.impl;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.config.FilingDataConfig;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.helper.LogMapHelper;
import uk.gov.companieshouse.pscverificationapi.model.FilingKind;
import uk.gov.companieshouse.pscverificationapi.service.FilingDataService;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;
import uk.gov.companieshouse.pscverificationapi.utils.MapHelper;

/**
 * Produces Filing Data format for consumption as JSON by filing-resource-handler external service.
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
    public FilingApi generatePscVerification(final String filingId, final Transaction transaction) {
        final var filingApi = new FilingApi();
        filingApi.setKind(
                FilingKind.PSC_VERIFICATION_INDIVIDUAL.getValue()); // TODO: handling other kinds to come later
        final var populatedFiling = populateFilingData(filingApi, filingId, transaction);
        populatedFiling.setDescription(filingDataConfig.getPscVerificationDescription());

        return filingApi;
    }

    private FilingApi populateFilingData(final FilingApi filing, final String filingId, final Transaction transaction) {

        final var transactionId = transaction.getId();
        final var logMap = LogMapHelper.createLogMap(transactionId, filingId);
        logger.debugContext(transactionId, "Fetching PSC verification", logMap);

        final var pscVerificationOpt = pscVerificationService.get(filingId);
        final var pscVerification = pscVerificationOpt.orElseThrow(() -> new FilingResourceNotFoundException(
                String.format("PSC verification not found when generating filing for %s", filingId)));

        final var dataMap = MapHelper.convertObject(pscVerification.getData(), PropertyNamingStrategies.SNAKE_CASE);

        logMap.put("Data to submit", dataMap);
        logger.debugContext(transactionId, filingId, logMap);

        filing.setData(dataMap);

        return filing;
    }
}