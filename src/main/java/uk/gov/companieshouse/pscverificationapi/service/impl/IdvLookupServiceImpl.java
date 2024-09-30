package uk.gov.companieshouse.pscverificationapi.service.impl;

import java.text.MessageFormat;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.identityverification.request.PrivateUvidMatchResourcePost;
import uk.gov.companieshouse.api.identityverification.model.UvidMatch;
import uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.exception.TransactionServiceException;
import uk.gov.companieshouse.pscverificationapi.exception.IdvLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.helper.LogMapHelper;
import uk.gov.companieshouse.pscverificationapi.service.IdvLookupService;

@Service
public class IdvLookupServiceImpl implements IdvLookupService {

    private static final String BASE_URI = "/verification";
    private static final String IDENTITY_BASE_URI = BASE_URI + "/identities";
    private static final String UVID_MATCH_URI_PART = "/uvid_match";
    private static final String UNEXPECTED_STATUS_CODE = "Unexpected Status Code received";

    private final ApiClientService apiClientService;
    private final Logger logger;

    /**
     * Interacts with the external CHS IDV API service to confirm a UVID match
     */
    public IdvLookupServiceImpl(ApiClientService apiClientService, final Logger logger) {
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    /**
     * Retrieves the matched UVID
     * @param uvidMatch the provided uvidMatch
     * @return uvidMatchResponse, if found
     * @throws IdvLookupServiceException if an error occurs within this service
     * @throws ApiErrorResponseException if the API responds with an error
     * @throws URIValidationException for a UVID validation error
     */
    @Override
    public UvidMatchResponse matchUvid(UvidMatch uvidMatch)
        throws IdvLookupServiceException, ApiErrorResponseException, URIValidationException {

        final var logMap = LogMapHelper.createLogMap(uvidMatch.getUvid());
        var uvidMatchUrl = IDENTITY_BASE_URI + UVID_MATCH_URI_PART;

        var internalApiClient = apiClientService.getInternalApiClient();
        PrivateUvidMatchResourcePost uvidMatchResourcePost =
            internalApiClient.privateIdentityVerificationResourceHandler()
                .matchUvid(uvidMatchUrl, uvidMatch);

        try {
            ApiResponse<UvidMatchResponse> uvidMatchResponse = uvidMatchResourcePost.execute();
            return uvidMatchResponse.getData();

        }
        catch (final URIValidationException e) {
            logger.errorContext(uvidMatch.getUvid(), UNEXPECTED_STATUS_CODE, e, logMap);
            throw new TransactionServiceException(
                MessageFormat.format("Error matching UVID {0}: {1}", uvidMatch.getUvid(),
                    e.getMessage()), e);
        }

    }
}
