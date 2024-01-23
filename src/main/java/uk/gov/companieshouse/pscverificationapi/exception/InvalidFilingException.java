package uk.gov.companieshouse.pscverificationapi.exception;

import java.util.List;
import org.springframework.validation.FieldError;

/**
 * A validation Exception with {@link FieldError}s produced by Spring MVC.
 */
public class InvalidFilingException extends RuntimeException {
    private final List<FieldError> fieldErrors;

    public InvalidFilingException(final List<FieldError> fieldErrors) {
        this.fieldErrors = List.copyOf(fieldErrors);
    }

    public List<FieldError> getFieldErrors() {
        return List.copyOf(fieldErrors);
    }
}
