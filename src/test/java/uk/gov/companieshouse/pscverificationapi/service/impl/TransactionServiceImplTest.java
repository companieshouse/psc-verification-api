package uk.gov.companieshouse.pscverificationapi.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.privatetransaction.PrivateTransactionResourceHandler;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionPatch;
import uk.gov.companieshouse.api.handler.transaction.TransactionsResourceHandler;
import uk.gov.companieshouse.api.handler.transaction.request.TransactionsGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.exception.TransactionServiceException;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private ApiClientService apiClientService;
    @Mock
    private Logger logger;
    @Mock
    private ApiClient apiClient;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private TransactionsResourceHandler transactionsResourceHandler;
    @Mock
    private PrivateTransactionResourceHandler privateTransactionResourceHandler;
    @Mock
    private TransactionsGet transactionGet;
    @Mock
    private PrivateTransactionPatch privateTransactionPatch;
    @Mock
    private ApiResponse<Transaction> transactionApiResponse;
    @Mock
    private ApiResponse<Void> voidApiResponse;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(apiClientService, logger);
    }

    @Test
    void getTransaction_success() throws Exception {
        String transactionId = "123";
        String header = "ERIC";
        Transaction transaction = new Transaction();

        when(apiClientService.getApiClient(header)).thenReturn(apiClient);
        when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
        when(transactionsResourceHandler.get("/transactions/" + transactionId)).thenReturn(transactionGet);
        when(transactionGet.execute()).thenReturn(transactionApiResponse);
        when(transactionApiResponse.getData()).thenReturn(transaction);

        Transaction result = transactionService.getTransaction(transactionId, header);

        assertThat(result, is(transaction));
        verify(logger).debugContext(eq(transactionId), contains("Retrieved transaction details"), anyMap());
    }

    @Test
    void getTransaction_apiError_throwsException() throws Exception {
        String transactionId = "123";
        String header = "ERIC";
        ApiErrorResponseException apiException = mock(ApiErrorResponseException.class);
        when(apiException.getStatusCode()).thenReturn(500);
        when(apiException.getStatusMessage()).thenReturn("Internal Error");

        when(apiClientService.getApiClient(header)).thenReturn(apiClient);
        when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
        when(transactionsResourceHandler.get("/transactions/" + transactionId)).thenReturn(transactionGet);
        when(transactionGet.execute()).thenThrow(apiException);

        assertThrows(TransactionServiceException.class, () ->
                transactionService.getTransaction(transactionId, header));
        verify(logger).errorContext(eq(transactionId), contains("Unexpected Status Code"), eq(apiException), anyMap());
    }

    @Test
    void getTransaction_uriValidation_throwsException() throws Exception {
        String transactionId = "123";
        String header = "ERIC";
        when(apiClientService.getApiClient(header)).thenReturn(apiClient);
        when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
        when(transactionsResourceHandler.get("/transactions/" + transactionId)).thenReturn(transactionGet);
        when(transactionGet.execute()).thenThrow(new URIValidationException("bad uri"));

        assertThrows(TransactionServiceException.class, () ->
                transactionService.getTransaction(transactionId, header));
    }

    @Test
    void updateTransaction_success() throws Exception {
        String header = "ERIC";
        Transaction transaction = new Transaction();
        transaction.setId("456");

        when(apiClientService.getInternalApiClient(header)).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.patch("/private/transactions/" + transaction.getId(), transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenReturn(voidApiResponse);
        when(voidApiResponse.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT.value());

        assertDoesNotThrow(() -> transactionService.updateTransaction(transaction, header));
        verify(logger).debugContext(eq(transaction.getId()), contains("Updating transaction"), anyMap());
    }

    @Test
    void updateTransaction_nonNoContent_throwsException() throws Exception {
        String header = "ERIC";
        Transaction transaction = new Transaction();
        transaction.setId("456");

        when(apiClientService.getInternalApiClient(header)).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.patch("/private/transactions/" + transaction.getId(), transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenReturn(voidApiResponse);
        when(voidApiResponse.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST.value());

        assertThrows(TransactionServiceException.class, () ->
                transactionService.updateTransaction(transaction, header));
    }

    @Test
    void updateTransaction_apiError_throwsException() throws Exception {
        String header = "ERIC";
        Transaction transaction = new Transaction();
        transaction.setId("456");
        ApiErrorResponseException apiException = mock(ApiErrorResponseException.class);
        when(apiException.getStatusCode()).thenReturn(500);
        when(apiException.getStatusMessage()).thenReturn("Internal Error");

        when(apiClientService.getInternalApiClient(header)).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.patch("/private/transactions/" + transaction.getId(), transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenThrow(apiException);

        assertThrows(TransactionServiceException.class, () ->
                transactionService.updateTransaction(transaction, header));
        verify(logger).errorContext(eq(transaction.getId()), contains("Unexpected Status Code"), eq(apiException), anyMap());
    }

    @Test
    void updateTransaction_uriValidation_throwsException() throws Exception {
        String header = "ERIC";
        Transaction transaction = new Transaction();
        transaction.setId("456");

        when(apiClientService.getInternalApiClient(header)).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.patch("/private/transactions/" + transaction.getId(), transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenThrow(new URIValidationException("bad uri"));

        assertThrows(TransactionServiceException.class, () ->
                transactionService.updateTransaction(transaction, header));
        verify(logger).errorContext(eq(transaction.getId()), contains("Unexpected Status Code"), any(Exception.class), anyMap());
    }

    @Test
    void updateTransaction_ioException_throwsException() throws Exception {
        String header = "ERIC";
        Transaction transaction = new Transaction();
        transaction.setId("456");

        when(apiClientService.getInternalApiClient(header)).thenThrow(new IOException("IO error"));

        assertThrows(TransactionServiceException.class, () ->
                transactionService.updateTransaction(transaction, header));
        verify(logger).errorContext(eq(transaction.getId()), contains("Unexpected Status Code"), any(Exception.class), anyMap());
    }

}