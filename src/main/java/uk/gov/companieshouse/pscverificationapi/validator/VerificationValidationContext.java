package uk.gov.companieshouse.pscverificationapi.validator;

import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public record VerificationValidationContext(@NonNull PscVerificationData dto,
                                            @NonNull List<FieldError> errors,
                                            @NonNull Transaction transaction,
                                            @NonNull PscType pscType,
                                            String passthroughHeader) {



    /**
     * @param dto               the DTO to validate
     * @param errors            the list of errors append; MUST be non-null and modifiable
     * @param transaction       the Transaction
     * @param pscType           the PSC type
     * @param passthroughHeader the request pass through header
     */
    public VerificationValidationContext(@NonNull final PscVerificationData dto,
        @NonNull final List<FieldError> errors, @NonNull final Transaction transaction,
        @NonNull final PscType pscType, final String passthroughHeader) {
        this.dto = dto;
        this.errors = errors;
        this.transaction = transaction;
        this.pscType = pscType;
        this.passthroughHeader = passthroughHeader;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VerificationValidationContext that = (VerificationValidationContext) o;
        return pscType == that.pscType && Objects.equals(dto, that.dto) && Objects.equals(errors,
            that.errors) && Objects.equals(transaction, that.transaction) && Objects.equals(
            passthroughHeader, that.passthroughHeader);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dto, errors, transaction, pscType, passthroughHeader);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", VerificationValidationContext.class.getSimpleName() + "[",
            "]").add("dto=" + dto)
            .add("errors=" + errors)
            .add("transaction=" + transaction)
            .add("pscType=" + pscType)
            .add("passthroughHeader='" + passthroughHeader + "'")
            .toString();
    }
}
