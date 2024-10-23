package uk.gov.companieshouse.pscverificationapi.validator;

import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Record representing a validation chain for a specific PSC (Person with Significant Control) type.
 *
 * @param pscType the type of PSC
 * @param first the first validator in the validation chain
 */
public record VerificationValidationChain(PscType pscType, VerificationValidator first) implements ValidationChainEnable {

    /**
     * Constructs a new {@code VerificationValidationChain} with the specified PSC type and first validator.
     *
     * @param pscType the type of PSC
     * @param first the first validator in the validation chain
     * @throws NullPointerException if {@code pscType} or {@code first} is null
     */
    public VerificationValidationChain(final PscType pscType, final VerificationValidator first) {
        this.pscType = Objects.requireNonNull(pscType);
        this.first = Objects.requireNonNull(first);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", VerificationValidationChain.class.getSimpleName() + "[",
            "]").add("pscType=" + pscType).add("first=" + first).toString();
    }
}
