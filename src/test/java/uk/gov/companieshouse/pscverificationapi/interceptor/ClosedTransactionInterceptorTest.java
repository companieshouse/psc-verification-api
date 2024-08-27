package uk.gov.companieshouse.pscverificationapi.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.AttributeName;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClosedTransactionInterceptorTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Object handler;
    @Mock
    private Transaction transaction;

    private ClosedTransactionInterceptor testClosedTransactionInterceptor;

    @BeforeEach
    void setUp() {
        testClosedTransactionInterceptor = new ClosedTransactionInterceptor("TestClosedTransactionInterceptor");
    }

    @Test
    void preHandleTransactionNull() {

        when(request.getAttribute(AttributeName.TRANSACTION.getValue())).thenReturn(null);
        var result = testClosedTransactionInterceptor.preHandle(request, response, handler);

        assertFalse(result);
    }

    @Test
    void preHandleTransactionClosed() {

        when(request.getAttribute(AttributeName.TRANSACTION.getValue())).thenReturn(transaction);
        when(transaction.getStatus()).thenReturn(TransactionStatus.CLOSED);
        var result = testClosedTransactionInterceptor.preHandle(request, response, handler);

        assertTrue(result);
    }

    @Test
    void preHandleTransactionOpen() {

        when(transaction.getStatus()).thenReturn(TransactionStatus.OPEN);
        when(request.getAttribute(AttributeName.TRANSACTION.getValue())).thenReturn(transaction);
        var result = testClosedTransactionInterceptor.preHandle(request, response, handler);

        assertFalse(result);
    }
}