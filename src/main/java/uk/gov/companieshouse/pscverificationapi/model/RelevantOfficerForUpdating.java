package uk.gov.companieshouse.pscverificationapi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Objects;
import java.util.StringJoiner;
import uk.gov.companieshouse.api.model.psc.NameElementsApi;

public final class RelevantOfficerForUpdating {
    @JsonMerge
    private final NameElementsApi nameElements;
    private final @Past LocalDate dateOfBirth;
    private final Boolean isEmployee;
    private final Boolean isDirector;

    public RelevantOfficerForUpdating(
        @JsonProperty final NameElementsApi nameElements,
        @JsonProperty @JsonFormat(pattern = "yyyy-MM-dd",
            timezone = "UTC") final LocalDate dateOfBirth,
        @JsonProperty final Boolean isEmployee,
        @JsonProperty final Boolean isDirector) {
        this.nameElements = nameElements;
        this.dateOfBirth = dateOfBirth;
        this.isEmployee = isEmployee;
        this.isDirector = isDirector;
    }

    public NameElementsApi getNameElements() {
        return nameElements;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Boolean getIsEmployee() {
        return isEmployee;
    }

    public Boolean getIsDirector() {
        return isDirector;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof RelevantOfficerForUpdating that)) return false;
        return Objects.equals(getNameElements(), that.getNameElements()) && Objects.equals(
            getDateOfBirth(), that.getDateOfBirth()) && Objects.equals(getIsEmployee(),
            that.getIsEmployee()) && Objects.equals(getIsDirector(), that.getIsDirector());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNameElements(), getDateOfBirth(), getIsEmployee(), getIsDirector());
    }

    @Override
    public String toString() {
        return "RelevantOfficerForUpdating[" + "nameElements=" + nameElementsToString(nameElements) + ", " +
            "dateOfBirth=" + dateOfBirth + ", " + "isEmployee=" + isEmployee + ", " + "isDirector"
            + "=" + isDirector + ']';
    }

    private String nameElementsToString(final NameElementsApi nameElements) {

        if (nameElements != null) {
            return new StringJoiner(", ", NameElementsApi.class.getSimpleName() + "[", "]").add(
                    "title='" + nameElements.getTitle() + "'")
                .add("forename='" + nameElements.getForename() + "'")
                .add("otherForenames='" + nameElements.getOtherForenames() + "'")
                .add("middleName='" + nameElements.getMiddleName() + "'")
                .add("surname='" + nameElements.getSurname() + "'")
                .toString();

        } else return null;

    }

}
