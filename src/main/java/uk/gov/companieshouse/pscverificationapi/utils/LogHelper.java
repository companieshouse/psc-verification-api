package uk.gov.companieshouse.pscverificationapi.utils;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Helper class for creating log property maps for structured logging.
 */
@Component
public final class LogHelper {

    private LogHelper() {
        // intentionally blank
    }

    public static Map<String, Object> createLogMap(String transactionId) {
        return createLogMap(transactionId, null);
    }

    /**
     * Create a mutable property map required by CH logging methods. Sets standard properties
     * 'transaction_id' and 'filing_id'.
     *
     * @param transactionId the Transaction ID
     * @param filingId      the PSC Filing ID
     * @return the new property map
     */
    public static Map<String, Object> createLogMap(String transactionId, String filingId) {
        final Map<String, Object> logMap = new HashMap<>();

        logMap.put("transaction_id", transactionId);
        logMap.put("filing_id", filingId);

        return logMap;
    }
}
