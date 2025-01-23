package uk.gov.companieshouse.pscverificationapi.sdk.companieshouse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.companieshouse.api.ApiClient;

class ApiClientServiceTest {

    @Mock
    ApiClient apiClient;

    private ApiClientService apiClientService;

    @BeforeEach
    void setUp() {
        apiClientService = new ApiClientService();
    }

    @Disabled("We can't get coverage here because of environment variables")
    @Test
    void getSdk() {
        when(apiClientService.getApiClient("key")).thenReturn(apiClient);
        assertThat(apiClient, isNotNull());
    }

}