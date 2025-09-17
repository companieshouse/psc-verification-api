package uk.gov.companieshouse.pscverificationapi.sdk.companieshouse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

/**
 * Service for retrieving CH internal API clients.
 */
@Service
public class InternalApiClientService {

    private String internalApiUrl;

    @Autowired
    public InternalApiClientService(@Value("${internal.api.url}") String internalApiUrl) {
        this.internalApiUrl = internalApiUrl;
    }

    public InternalApiClient getInternalApiClient() {
        final var internalApiClient = ApiSdkManager.getInternalSDK();
        internalApiClient.setInternalBasePath(internalApiUrl);
        return internalApiClient;
    }
}
