package uk.gov.companieshouse.pscverificationapi.sdk.companieshouse;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.sdk.manager.ApiClientManager;

@Service
public class InternalApiClientService {

    private final EnvironmentReader environmentReader;

    public InternalApiClientService(EnvironmentReader environmentReader) {
        this.environmentReader = environmentReader;
    }

    public InternalApiClient getInternalApiClient() {
        final var internalApiClient = ApiClientManager.getPrivateSDK();
        internalApiClient.setInternalBasePath(environmentReader.getMandatoryString("INTERNAL_API_URL"));
        return internalApiClient;
    }
}
