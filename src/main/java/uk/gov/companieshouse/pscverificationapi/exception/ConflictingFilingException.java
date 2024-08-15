package uk.gov.companieshouse.pscverificationapi.exception;

import org.springframework.validation.FieldError;

import java.util.List;

/**
 * Filing not allowed exception.
 */
public class ConflictingFilingException extends InvalidFilingException {
    public ConflictingFilingException(List<FieldError> fieldErrors) {
        super(fieldErrors);
    }
}
