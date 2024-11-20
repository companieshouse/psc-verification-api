package uk.gov.companieshouse.pscverificationapi.service;

import uk.gov.companieshouse.api.identityverification.model.UvidMatch;
import uk.gov.companieshouse.api.model.psc.PscApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.psc.IndividualFullRecord;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.exception.PscLookupServiceException;

/**
 * Interacts with the external CHS PSC API service to retrieve PSCs.
 */
public interface PscLookupService {
    /**
     * Retrieve a PSC by PscVerificationData.
     *
     * @param transaction       the Transaction
     * @param data              the PSC verification data
     * @param pscType           the PSC Type
     * @param ericPassThroughHeader includes authorisation for transaction fetch
     * @return the PSC details, if found
     * @throws PscLookupServiceException if the PSC was not found or an error occurred
     */
    PscApi getPsc(Transaction transaction, PscVerificationData data, PscType pscType, final String ericPassThroughHeader)
        throws PscLookupServiceException;

    /**
     * Retrieve a PSC Individual Full Record by PscVerificationData.
     *
     * @param transaction           the Transaction
     * @param data                  the PSC verification data
     * @param pscType               the PSC Type
     * @return the PSC Full Record details, if found
     * @throws PscLookupServiceException if the PSC was not found or an error occurred
     */
    IndividualFullRecord getPscIndividualFullRecord(Transaction transaction, PscVerificationData data, PscType pscType)
            throws PscLookupServiceException;

    /**
     * Retrieve a UvidMatch with the PSC data.
     *
     * @param transaction       the Transaction
     * @param data              the PSC verification data
     * @param pscType           the PSC Type
     * @param ericPassThroughHeader includes authorisation for transaction fetch
     * @return the UvidMatch, if found
     * @throws PscLookupServiceException if the PSC was not found or an error occurred
     */
    UvidMatch getUvidMatchWithPscData(Transaction transaction, PscVerificationData data, PscType pscType, final String ericPassThroughHeader)
        throws PscLookupServiceException;

}
