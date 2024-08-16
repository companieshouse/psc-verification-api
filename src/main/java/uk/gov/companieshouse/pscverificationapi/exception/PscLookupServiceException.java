package uk.gov.companieshouse.pscverificationapi.exception;

/**
 * PSC Service query failed.
 */
public class PscLookupServiceException extends RuntimeException {

    public PscLookupServiceException(final String s, final Exception e) {
        super(s, e);
    }
}
