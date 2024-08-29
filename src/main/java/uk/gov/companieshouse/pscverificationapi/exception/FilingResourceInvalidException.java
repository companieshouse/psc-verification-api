package uk.gov.companieshouse.pscverificationapi.exception;

/**
 * PSC Verification resource is found, but invalid for verification.
 */
public class FilingResourceInvalidException extends RuntimeException {

    public FilingResourceInvalidException(final String message) {
        super(message);
    }

    public FilingResourceInvalidException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
