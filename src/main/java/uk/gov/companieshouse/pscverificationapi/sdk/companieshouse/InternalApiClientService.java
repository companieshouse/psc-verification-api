package uk.gov.companieshouse.pscverificationapi.sdk.companieshouse;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.sdk.manager.ApiClientManager;

@Service
public class InternalApiClientService {

    public InternalApiClient getInternalApiClient() {
        return ApiClientManager.getPrivateSDK();
    }
}
