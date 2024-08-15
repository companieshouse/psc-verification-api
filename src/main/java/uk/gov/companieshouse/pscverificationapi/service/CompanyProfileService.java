package uk.gov.companieshouse.pscverificationapi.service;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.exception.CompanyProfileServiceException;

/**
 * The company profile service layer responsible for
 * retrieving company profile data from Company Profile API.
 */
public interface CompanyProfileService {

    /**
     * Query the company profile service for a given transaction.
     *
     * @param transaction the transaction
     * @param ericPassThroughHeader includes authorisation details
     * @return the company profile if found
     * @throws CompanyProfileServiceException if not found or an error occurred
     */
    CompanyProfileApi getCompanyProfile(final Transaction transaction, final PscVerificationData pscVerificationData, final String ericPassThroughHeader)
            throws CompanyProfileServiceException;

}
