package uk.gov.companieshouse.pscverificationapi.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.identityverification.PrivateIdentityVerificationResourceHandler;
import uk.gov.companieshouse.api.handler.identityverification.request.PrivateUvidMatchResourcePost;
import uk.gov.companieshouse.api.identityverification.model.UvidMatch;
import uk.gov.companieshouse.api.identityverification.model.UvidMatchResponse;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.pscverificationapi.exception.TransactionServiceException;

@ExtendWith(MockitoExtension.class)
class IdvLookupServiceImplTest {

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private PrivateIdentityVerificationResourceHandler privateIdentityVerificationResourceHandler;

    @Mock
    private PrivateUvidMatchResourcePost privateUvidMatchResourcePost;

    @InjectMocks
    private IdvLookupServiceImpl uvidMatchService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testMatchUvidSuccess() throws Exception {
        UvidMatch uvidMatch = new UvidMatch();
        uvidMatch.setUvid("testUvid");

        UvidMatchResponse expectedResponse = new UvidMatchResponse();
        ApiResponse<UvidMatchResponse> apiResponse = new ApiResponse<>();
        apiResponse.(expectedResponse);

        when(apiClientService.getInternalApiClient().privateIdentityVerificationResourceHandler()
            .matchUvid(anyString(), eq(uvidMatch))).thenReturn(privateUvidMatchResourcePost);
        when(privateUvidMatchResourcePost.execute()).thenReturn(apiResponse);

        UvidMatchResponse actualResponse = uvidMatchService.matchUvid(uvidMatch);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testMatchUvidUriValidationException() throws Exception {
        UvidMatch uvidMatch = new UvidMatch();
        uvidMatch.setUvid("testUvid");

        when(apiClientService.getInternalApiClient().privateIdentityVerificationResourceHandler()
            .matchUvid(anyString(), eq(uvidMatch))).thenReturn(privateUvidMatchResourcePost);
        when(privateUvidMatchResourcePost.execute()).thenThrow(new URIValidationException("Invalid URI"));

        assertThrows(TransactionServiceException.class, () -> {
            uvidMatchService.matchUvid(uvidMatch);
        });
    }

}