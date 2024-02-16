package uk.gov.companieshouse.pscverificationapi.model;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public final class PscVerificationDataForUpdating {
    private final String companyNumber;
    private final String pscAppointmentId;
    @JsonMerge
    private final RelevantOfficerForUpdating relevantOfficer;
    @JsonMerge
    private final VerificationDetailsForUpdating verificationDetails;

    public PscVerificationDataForUpdating(@JsonProperty final String companyNumber,
        @JsonProperty final String pscAppointmentId,
        @JsonProperty final RelevantOfficerForUpdating relevantOfficer,
        @JsonProperty final VerificationDetailsForUpdating verificationDetails) {
        this.companyNumber = companyNumber;
        this.pscAppointmentId = pscAppointmentId;
        this.relevantOfficer = relevantOfficer;
        this.verificationDetails = verificationDetails;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public String getPscAppointmentId() {
        return pscAppointmentId;
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
            getPscAppointmentId(), that.getPscAppointmentId()) && Objects.equals(
            getRelevantOfficer(), that.getRelevantOfficer()) && Objects.equals(
            getVerificationDetails(), that.getVerificationDetails());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCompanyNumber(), getPscAppointmentId(), getRelevantOfficer(),
            getVerificationDetails());
    }

    @Override
    public String toString() {
        return "PscVerificationDataForUpdating[" + "companyNumber=" + companyNumber + ", " +
            "pscAppointmentId=" + pscAppointmentId + ", " + "relevantOfficer=" + relevantOfficer + ", " + "verificationDetails=" + verificationDetails + ']';
    }

}
