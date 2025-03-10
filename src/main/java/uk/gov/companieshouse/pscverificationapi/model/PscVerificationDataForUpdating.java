package uk.gov.companieshouse.pscverificationapi.model;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public final class PscVerificationDataForUpdating {
    private final String companyNumber;
    private final String pscNotificationId;
    @JsonMerge
    private final RelevantOfficerForUpdating relevantOfficer;
    @JsonMerge
    private final VerificationDetailsForUpdating verificationDetails;

    public PscVerificationDataForUpdating(@JsonProperty final String companyNumber,
        @JsonProperty final String pscNotificationId,
        @JsonProperty final RelevantOfficerForUpdating relevantOfficer,
        @JsonProperty final VerificationDetailsForUpdating verificationDetails) {
        this.companyNumber = companyNumber;
        this.pscNotificationId = pscNotificationId;
        this.relevantOfficer = relevantOfficer;
        this.verificationDetails = verificationDetails;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public String getPscNotificationId() {
        return pscNotificationId;
    }

    public RelevantOfficerForUpdating getRelevantOfficer() {
        return relevantOfficer;
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
            getRelevantOfficer(), that.getRelevantOfficer()) && Objects.equals(
            getVerificationDetails(), that.getVerificationDetails());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCompanyNumber(), getPscNotificationId(), getRelevantOfficer(),
            getVerificationDetails());
    }

    @Override
    public String toString() {
        return "PscVerificationDataForUpdating[" + "companyNumber=" + companyNumber + ", " +
            "pscNotificationId=" + pscNotificationId + ", " + "relevantOfficer=" + relevantOfficer + ", " + "verificationDetails=" + verificationDetails + ']';
    }

}
