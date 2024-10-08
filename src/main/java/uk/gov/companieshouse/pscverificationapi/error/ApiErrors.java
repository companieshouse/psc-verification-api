package uk.gov.companieshouse.pscverificationapi.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import uk.gov.companieshouse.api.error.ApiError;

/**
 * Wrapper collection for distinct {@link ApiError}s to return in validation responses.
 */
public class ApiErrors {
    private final Set<ApiError> errors;

    public ApiErrors() {
        this.errors = new HashSet<>();
    }

    public ApiErrors(final Collection<ApiError> errors) {
        this.errors = new HashSet<>(errors);
    }

    /**
     * Add a new error to the error set.
     *
     * @param error the APIError
     * @return true if the error set changed as a result
     */
    public boolean add(final ApiError error) {
        Objects.requireNonNull(error, "'error' cannot be null");

        return errors.add(error);
    }

    /**
     * Add a collection of new error to the error set.
     *
     * @param moreErrors the APIError collection to add
     */
    public void addAll(final Collection<ApiError> moreErrors) {
        Objects.requireNonNull(errors, "'errors' cannot be null");

        errors.addAll(moreErrors);
    }

    /**
     * Checks whether the APIError set is empty.
     *
     * @return false if there are no errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Checks whether the error is already present.
     *
     * @param error the APIError to be checked
     * @return true if the set contains the specified error
     */
    public boolean contains(final ApiError error) {
        Objects.requireNonNull(error, "'error' cannot be null");

        return errors.contains(error);
    }

    public Set<ApiError> getErrors() {
        return Collections.unmodifiableSet(errors);
    }

    @JsonIgnore
    public int getErrorCount() {
        return errors.size();
    }

}
