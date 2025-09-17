package uk.gov.companieshouse.pscverificationapi.model;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Data class for updating PSC verification details.
 * <p>
 * Holds company number, PSC notification ID, and verification details for update operations.
 * </p>
 */
public final class PscVerificationDataForUpdating {
    private final String companyNumber;
    private final String pscNotificationId;
    @JsonMerge
    private final VerificationDetailsForUpdating verificationDetails;

    public PscVerificationDataForUpdating(@JsonProperty final String companyNumber,
        @JsonProperty final String pscNotificationId,
        @JsonProperty final VerificationDetailsForUpdating verificationDetails) {
        this.companyNumber = companyNumber;
        this.pscNotificationId = pscNotificationId;
        this.verificationDetails = verificationDetails;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public String getPscNotificationId() {
        return pscNotificationId;
    }

    public VerificationDetailsForUpdating getVerificationDetails() {
        return verificationDetails;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PscVerificationDataForUpdating that)) return false;
        return Objects.equals(getCompanyNumber(), that.getCompanyNumber()) && Objects.equals(
            getPscNotificationId(), that.getPscNotificationId()) && Objects.equals(
            getVerificationDetails(), that.getVerificationDetails());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCompanyNumber(), getPscNotificationId(),
            getVerificationDetails());
    }

    @Override
    public String toString() {
        return "PscVerificationDataForUpdating[" + "companyNumber=" + companyNumber + ", " +
            "pscNotificationId=" + pscNotificationId + ", " + "verificationDetails=" + verificationDetails + ']';
    }

}
