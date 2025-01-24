package uk.gov.companieshouse.pscverificationapi.sdk.companieshouse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalApiClientServiceTest {

    private InternalApiClientService service;

    @Mock
    private InternalApiClient internalApiClient;

    @BeforeEach
    void setUp() {
        service = new InternalApiClientService("API_URL");
    }

    @Test
    @DisplayName("getInternalApiClient")
    void getInternalApiClient() {

        try (final MockedStatic<ApiSdkManager> apiSdkManager = Mockito.mockStatic(ApiSdkManager.class)) {
            apiSdkManager.when(ApiSdkManager::getInternalSDK).thenReturn(internalApiClient);
            try (MockedConstruction<EnvironmentReaderImpl> mocked = mockConstruction(EnvironmentReaderImpl.class)) {

                // Given
                final EnvironmentReader environmentReader = new EnvironmentReaderImpl();
                when(environmentReader.getMandatoryString("INTERNAL_API_URL")).thenReturn("INTERNAL_API_URL");

                // When
                final InternalApiClient internalApiClientReturned = service.getInternalApiClient();

                // Then
                apiSdkManager.verify(ApiSdkManager::getInternalSDK);
                verify(internalApiClient).setInternalBasePath("API_URL");
                assertThat(internalApiClientReturned, is(internalApiClient));
                assertThat(mocked.constructed(), hasSize(1));
                assertThat(mocked.constructed().getFirst().getMandatoryString("INTERNAL_API_URL"), is("INTERNAL_API_URL") );
            }
        }
    }
}