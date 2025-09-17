package uk.gov.companieshouse.pscverificationapi.enumerations;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Enum for PSC types.
 */
public enum PscType {
    INDIVIDUAL("individual");

    PscType(final String value) {
        this.value = value;
    }

    private final String value;

    public String getValue() {
        return value;
    }

    public static Optional<PscType> nameOf(final String value) {
        return EnumSet.allOf(PscType.class).stream().filter(v -> v.getValue().equals(value)).findAny();
    }
}
