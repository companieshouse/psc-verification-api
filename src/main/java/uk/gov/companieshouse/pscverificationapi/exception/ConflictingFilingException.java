package uk.gov.companieshouse.pscverificationapi.exception;

import java.util.List;
import org.springframework.validation.FieldError;

/**
 * Filing not allowed exception.
 */
public class ConflictingFilingException extends InvalidFilingException {
    public ConflictingFilingException(List<FieldError> fieldErrors) {
        super(fieldErrors);
    }
}
