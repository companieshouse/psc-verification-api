package uk.gov.companieshouse.pscverificationapi.service.impl;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.identityverification.PrivateIdentityVerificationResourceHandler;
import uk.gov.companieshouse.api.handler.identityverification.request.PrivateUvidMatchResourcePost;
import uk.gov.companieshouse.api.identityverification.model.UvidMatch;
import uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.exception.TransactionServiceException;
import uk.gov.companieshouse.pscverificationapi.sdk.companieshouse.InternalApiClientService;
import uk.gov.companieshouse.pscverificationapi.service.IdvLookupService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdvLookupServiceImplTest {

    @Mock
    private InternalApiClientService internalApiClientService;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private PrivateIdentityVerificationResourceHandler privateIdentityVerificationResourceHandler;

    @Mock
    private PrivateUvidMatchResourcePost privateUvidMatchResourcePost;

    @Mock
    private ApiResponse<UvidMatchResponse> uvidMatchResponseApiResponse;

    @Mock
    private UvidMatch uvidMatch;

    @Mock
    Logger logger;

    private IdvLookupService uvidMatchService;

    @BeforeEach
    public void setUp() {
        uvidMatchService = new IdvLookupServiceImpl(logger, internalApiClientService);
    }

    @Test
    void testMatchUvidSuccess() throws Exception {

        when(uvidMatch.getUvid()).thenReturn("testUvid");

        UvidMatchResponse expectedResponse = new UvidMatchResponse();

        when(internalApiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateIdentityVerificationResourceHandler())
            .thenReturn(privateIdentityVerificationResourceHandler);
        when(privateIdentityVerificationResourceHandler.matchUvid(anyString(), eq(uvidMatch)))
            .thenReturn(privateUvidMatchResourcePost);
        when(privateUvidMatchResourcePost.execute()).thenReturn(uvidMatchResponseApiResponse);
        when(uvidMatchResponseApiResponse.getData()).thenReturn(expectedResponse);

        UvidMatchResponse actualResponse = uvidMatchService.matchUvid(uvidMatch);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void testMatchUvidUriValidationException() throws Exception {

        when(uvidMatch.getUvid()).thenReturn("testUvid");

        when(internalApiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateIdentityVerificationResourceHandler())
            .thenReturn(privateIdentityVerificationResourceHandler);
        when(privateIdentityVerificationResourceHandler.matchUvid(anyString(), eq(uvidMatch)))
            .thenReturn(privateUvidMatchResourcePost);

        when(privateUvidMatchResourcePost.execute()).thenThrow(new URIValidationException("Invalid URI"));

        assertThrows(TransactionServiceException.class, () -> {
            uvidMatchService.matchUvid(uvidMatch);
        });
    }

}