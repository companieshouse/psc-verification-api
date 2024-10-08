package uk.gov.companieshouse.pscverificationapi.service.impl;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.identityverification.PrivateIdentityVerificationResourceHandler;
import uk.gov.companieshouse.api.handler.identityverification.request.PrivateUvidMatchResourcePost;
import uk.gov.companieshouse.api.identityverification.model.UvidMatch;
import uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.sdk.ApiClientService;

@ExtendWith(MockitoExtension.class)
class IdvLookupServiceImplTest {

    @Mock
    private ApiClientService apiClientService;

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
    private IdvLookupServiceImpl uvidMatchService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

//    @Test
//    public void testMatchUvidSuccess() throws Exception {
//
//        when(uvidMatch.getUvid()).thenReturn("testUvid");
//
//        UvidMatchResponse expectedResponse = new UvidMatchResponse();
//
//        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
//        when(internalApiClient.privateIdentityVerificationResourceHandler())
//            .thenReturn(privateIdentityVerificationResourceHandler);
//        when(privateIdentityVerificationResourceHandler.matchUvid(anyString(), eq(uvidMatch)))
//            .thenReturn(privateUvidMatchResourcePost);
//        when(privateUvidMatchResourcePost.execute()).thenReturn(uvidMatchResponseApiResponse);
//        when(uvidMatchResponseApiResponse.getData()).thenReturn(expectedResponse);
//
//        UvidMatchResponse actualResponse = uvidMatchService.matchUvid(uvidMatch);
//
//        assertEquals(expectedResponse, actualResponse);
//    }
//
//    @Test
//    public void testMatchUvidUriValidationException() throws Exception {
//        UvidMatch uvidMatch = new UvidMatch();
//        uvidMatch.setUvid("testUvid");
//
//        when(apiClientService.getInternalApiClient().privateIdentityVerificationResourceHandler()
//            .matchUvid(anyString(), eq(uvidMatch))).thenReturn(privateUvidMatchResourcePost);
//        when(privateUvidMatchResourcePost.execute()).thenThrow(new URIValidationException("Invalid URI"));
//
//        assertThrows(TransactionServiceException.class, () -> {
//            uvidMatchService.matchUvid(uvidMatch);
//        });
//    }

}