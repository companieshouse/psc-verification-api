package uk.gov.companieshouse.pscverificationapi.sdk.companieshouse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.sdk.manager.ApiClientManager;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Service
public class InternalApiClientService {

    private String internalApiUrl;
    private final Logger logger;

    @Autowired
    public InternalApiClientService(@Value("${internal.api.url}") String internalApiUrl, Logger logger) {
        this.internalApiUrl = internalApiUrl;
        this.logger = logger;
        logger.info("internalApiUrl ..... " + internalApiUrl);
    }

    public InternalApiClient getInternalApiClient() {
        final var internalApiClient = ApiSdkManager.getInternalSDK();
        internalApiClient.setInternalBasePath(internalApiUrl);
        return internalApiClient;
    }
}
