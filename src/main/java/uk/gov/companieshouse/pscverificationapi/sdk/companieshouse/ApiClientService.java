package uk.gov.companieshouse.pscverificationapi.sdk.companieshouse;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.sdk.manager.ApiClientManager;

/**
 * Service for retrieving CH API clients.
 */
@Service
public class ApiClientService {

    public ApiClient getApiClient(String key) {
        return ApiClientManager.getSDK(key);
    }
}
