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
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.sdk.manager.ApiClientManager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalApiClientServiceTest {

    private static final String CONFIGURED_INTERNAL_API_URL = "http://api.chs.local:4001";

    private InternalApiClientService service;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private EnvironmentReader environmentReader;

    @BeforeEach
    void setUp() {
        service = new InternalApiClientService(environmentReader);
    }

    @Test
    @DisplayName("getInternalApiClient returns internal API client with an overridden internal API URL value")
    void getInternalApiClientOverridesInternalApiUrlUsed() {

        try (final MockedStatic<ApiClientManager> apiClientManager = Mockito.mockStatic(ApiClientManager.class)) {
            // Given
            apiClientManager.when(ApiClientManager::getPrivateSDK).thenReturn(internalApiClient);
            when(environmentReader.getMandatoryString("INTERNAL_API_URL")).thenReturn(CONFIGURED_INTERNAL_API_URL);

            // When
            final InternalApiClient internalApiClientReturned = service.getInternalApiClient();

            // Then
            verify(internalApiClient).setInternalBasePath(CONFIGURED_INTERNAL_API_URL);
            assertThat(internalApiClientReturned, is(internalApiClient));
        }
    }
}