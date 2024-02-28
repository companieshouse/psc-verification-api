package uk.gov.companieshouse.pscverificationapi.model;

import java.util.EnumSet;
import java.util.Optional;

public enum FilingKind {

    PSC_VERIFICATION_INDIVIDUAL("psc-verification#psc-verification-individual"),
    PSC_VERIFICATION_RLE_RO("psc-verification#psc-verification-rle-ro");

    FilingKind(final String value) {
        this.value = value;
    }

    private final String value;

    public String getValue() {
        return value;
    }

    public static Optional<FilingKind> nameOf(final String value) {
        return EnumSet.allOf(FilingKind.class).stream().filter(v -> v.getValue().equals(value)).findAny();
    }
}
