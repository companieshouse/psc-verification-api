package uk.gov.companieshouse.pscverificationapi.exception;

/**
 * Transaction not found or external query failed.
 */
public class TransactionServiceException extends RuntimeException {

    public TransactionServiceException(final String s, final Exception e) {
        super(s, e);
    }
}
