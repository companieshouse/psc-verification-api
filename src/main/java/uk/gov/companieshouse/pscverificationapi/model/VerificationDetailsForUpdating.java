package uk.gov.companieshouse.pscverificationapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Set;
import uk.gov.companieshouse.api.model.pscverification.NameMismatchReasonConstants;
import uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants;

/**
 * Data class for updating verification details.
 * <p>
 * Holds UVID, name mismatch reason, and verification statements for update operations.
 * </p>
 */
public final class VerificationDetailsForUpdating {
    private String uvid;
    private NameMismatchReasonConstants nameMismatchReason;
    private Set<VerificationStatementConstants> verificationStatements;

    @JsonCreator
    public VerificationDetailsForUpdating(@JsonProperty("uvid") final String uvid,
        @JsonProperty("name_mismatch_reason") final NameMismatchReasonConstants nameMismatchReason,
        @JsonProperty(
            "verification_statements") final Set<VerificationStatementConstants> statements) {
        this.uvid = uvid;
        this.nameMismatchReason = nameMismatchReason;
        this.verificationStatements = statements;
    }

    public String getUvid() {
        return uvid;
    }

    public void setUvid(final String uvid) {
        this.uvid = uvid;
    }

    public NameMismatchReasonConstants getNameMismatchReason() {
        return nameMismatchReason;
    }

    public void setNameMismatchReason(final NameMismatchReasonConstants nameMismatchReason) {
        this.nameMismatchReason = nameMismatchReason;
    }

    public Set<VerificationStatementConstants> getVerificationStatements() {
        return verificationStatements;
    }

    public void setVerificationStatements(final Set<VerificationStatementConstants> verificationStatements) {
        this.verificationStatements = verificationStatements;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof VerificationDetailsForUpdating that)) return false;
        return Objects.equals(getUvid(),
            that.getUvid()) && getNameMismatchReason() == that.getNameMismatchReason() && Objects.equals(
            getVerificationStatements(), that.getVerificationStatements());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUvid(), getNameMismatchReason(), getVerificationStatements());
    }

    @Override
    public String toString() {
        return "VerificationDetailsForUpdating[" + "uvid=" + uvid + ", " + "nameMismatchReason=" + nameMismatchReason + ", " + "statements=" + verificationStatements + ']';
    }

}
