package uk.gov.companieshouse.pscverificationapi.sdk.companieshouse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.sdk.manager.ApiClientManager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class InternalApiClientServiceTest {

    private InternalApiClientService service;

    @Mock
    private InternalApiClient internalApiClient;

    @BeforeEach
    void setUp() {
        service = new InternalApiClientService("DummyUrl");
    }

    @Test
    @DisplayName("getInternalApiClient")
    void getInternalApiClient() {

        try (final MockedStatic<ApiClientManager> apiClientManager = Mockito.mockStatic(ApiClientManager.class)) {
            // Given
            apiClientManager.when(ApiClientManager::getPrivateSDK).thenReturn(internalApiClient);

            // When
            final InternalApiClient internalApiClientReturned = service.getInternalApiClient();

            // Then
            assertThat(internalApiClientReturned, is(internalApiClient));
        }
    }
}