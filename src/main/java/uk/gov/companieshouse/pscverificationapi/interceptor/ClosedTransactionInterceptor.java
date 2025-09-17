package uk.gov.companieshouse.pscverificationapi.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.api.AttributeName;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Interceptor that allows filings-based requests only for closed transactions.
 * <p>
 * Checks the transaction status and blocks requests if the transaction is not closed,
 * returning a 403 status code and logging the event.
 * </p>
 */
public class ClosedTransactionInterceptor implements HandlerInterceptor {
    private final Logger logger;

    public ClosedTransactionInterceptor(String loggingNamespace) {
        this.logger = LoggerFactory.getLogger(loggingNamespace);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Transaction transaction = (Transaction) request.getAttribute(AttributeName.TRANSACTION.getValue());
        if (transaction != null && TransactionStatus.CLOSED.getStatus().equalsIgnoreCase(transaction.getStatus().getStatus())) {
            return true;
        } else {
            Map<String, Object> debugMap = new HashMap<>();
            debugMap.put("request_method", request.getMethod());
            this.logger.errorRequest(request, "ClosedTransactionInterceptor error: " +
                "transaction is not closed. The filings cannot be generated.", debugMap);
            response.setStatus(403);
            return false;
        }
    }
}
