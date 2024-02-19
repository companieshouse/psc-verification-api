package uk.gov.companieshouse.pscverificationapi.exception;

import java.util.List;
import org.springframework.validation.FieldError;

/**
 * Invalid data in PATCH request.
 */
public class InvalidPatchException extends InvalidFilingException {

    public InvalidPatchException(final List<FieldError> fieldErrors) {
        super(fieldErrors);
    }
}
