package uk.gov.companieshouse.pscverificationapi.sdk.companieshouse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import uk.gov.companieshouse.sdk.manager.ApiClientManager;

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

    @Disabled
    @Test
    @DisplayName("getInternalApiClient")
    void getInternalApiClient() {

        try (final MockedStatic<ApiClientManager> apiClientManager = Mockito.mockStatic(ApiClientManager.class)) {
            try (MockedConstruction<EnvironmentReaderImpl> mocked = mockConstruction(EnvironmentReaderImpl.class)) {

                // Given
                final EnvironmentReader environmentReader = new EnvironmentReaderImpl();
                when(environmentReader.getMandatoryString("INTERNAL_API_URL")).thenReturn("API_URL");

                apiClientManager.when(ApiClientManager::getPrivateSDK).thenReturn(internalApiClient);

                // When
                final InternalApiClient internalApiClientReturned = service.getInternalApiClient();

                // Then
                verify(internalApiClient).setInternalBasePath("API_URL");
                assertThat(internalApiClientReturned, is(internalApiClient));
                assertThat(internalApiClientReturned.getInternalBasePath(), is("API_URL"));
                assertThat(mocked.constructed(), hasSize(1));
                assertThat(mocked.constructed().getFirst().getMandatoryString("INTERNAL_API_URL"), is("API_URL") );
            }
        }
    }
}