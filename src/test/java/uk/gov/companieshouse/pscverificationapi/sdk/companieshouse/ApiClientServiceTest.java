package uk.gov.companieshouse.pscverificationapi.sdk.companieshouse;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.sdk.manager.ApiClientManager;

@ExtendWith(MockitoExtension.class)
class ApiClientServiceTest {

    @Mock
    ApiClient apiClient;

    private ApiClientService apiClientService;

    @BeforeEach
    void setUp() {
        apiClientService = new ApiClientService();
    }

    @Test
    void getSdk() {
        try (final MockedStatic<ApiClientManager> apiClientManager = Mockito.mockStatic(ApiClientManager.class)) {
            apiClientManager.when(ApiClientManager::getSDK).thenReturn(apiClient);
            try (MockedConstruction<EnvironmentReaderImpl> mocked = mockConstruction(EnvironmentReaderImpl.class)) {

                // Given
                final EnvironmentReader environmentReader = new EnvironmentReaderImpl();
                when(environmentReader.getMandatoryString("API_URL")).thenReturn("API_URL");

                // When
                final ApiClient apiClientReturned = apiClientService.getApiClient("API_URL");

                // Then
                assertThat(apiClientReturned, is(apiClient));
                assertThat(mocked.constructed(), hasSize(1));
                assertThat(mocked.constructed().getFirst().getMandatoryString("API_URL"), is("API_URL") );
            }
        }
    }

}