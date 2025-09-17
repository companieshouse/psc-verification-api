package uk.gov.companieshouse.pscverificationapi.service;

import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.identityverification.model.UvidMatch;
import uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse;
import uk.gov.companieshouse.pscverificationapi.exception.IdvLookupServiceException;

/**
 * Interacts with the external CHS IDV API service to confirm a UVID match
 */
public interface IdvLookupService {
    /**
     * Retrieves the matched UVID
     * 
     * @param uvidMatch the provided uvidMatch
     * @return uvidMatchResponse, if found
     * @throws IdvLookupServiceException if an error occurs within this service
     * @throws ApiErrorResponseException if the API responds with an error
     */
    UvidMatchResponse matchUvid(UvidMatch uvidMatch) throws IdvLookupServiceException,
        ApiErrorResponseException;
}
