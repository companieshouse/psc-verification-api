package uk.gov.companieshouse.pscverificationapi.helper;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public final class LogMapHelper {

    private LogMapHelper() {
        // intentionally blank
    }

    public static Map<String, Object> createLogMap(final String transactionId) {
        return createLogMap(transactionId, null);
    }

    /**
     * Create a mutable property map required by CH logging methods. Sets standard properties
     * 'transaction_id' and 'filing_id'.
     *
     * @param transactionId the Transaction ID
     * @param filingId      the Filing Resource ID
     * @return the new property map
     */
    public static Map<String, Object> createLogMap(final String transactionId,
        final String filingId) {
        final Map<String, Object> logMap = new HashMap<>();

        logMap.put("transaction_id", transactionId);
        logMap.put("filing_id", filingId);

        return logMap;
    }
}
