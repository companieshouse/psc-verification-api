package uk.gov.companieshouse.pscverificationapi.service;

import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;

/**
 * Produces Filing Data format for consumption as JSON by filing-resource-handler external service.
 */
public interface FilingDataService {

    /**
     * Generate the FilingApi object for the given filing id and transaction.
     * 
     * @param filingId    the PSC Filing id
     * @param transaction the transaction for the filing
     * @return the filing data details
     */
    FilingApi generateFilingApi(String filingId, Transaction transaction);
}
