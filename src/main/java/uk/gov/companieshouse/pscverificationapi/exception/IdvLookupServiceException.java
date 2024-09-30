package uk.gov.companieshouse.pscverificationapi.exception;

/**
 * Identity Verification Lookup Service query failed.
 */
public class IdvLookupServiceException extends RuntimeException {

    public IdvLookupServiceException(final String s, final Exception e) {
        super(s, e);
    }

}
