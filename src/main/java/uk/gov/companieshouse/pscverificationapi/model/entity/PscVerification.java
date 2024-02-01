package uk.gov.companieshouse.pscverificationapi.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;
import java.util.StringJoiner;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationLinks;

@Document(collection = "psc_verification")
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PscVerification implements Touchable{
    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant updatedAt;
    @JsonProperty(access= JsonProperty.Access.READ_ONLY)
    private PscVerificationLinks links;
    @JsonMerge
    private PscVerificationData data;

    public PscVerification() {
        // required by Spring Data
    }

    private PscVerification(Builder builder) {
        setId(builder.id);
        setCreatedAt(builder.createdAt);
        setUpdatedAt(builder.updatedAt);
        setLinks(builder.links);
        setData(builder.data);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(PscVerification copy) {
        final Builder builder = new Builder();
        builder.id = copy.getId();
        builder.createdAt = copy.getCreatedAt();
        builder.updatedAt = copy.getUpdatedAt();
        builder.links = copy.getLinks();
        builder.data = copy.getData();
        return builder;
    }

    public String getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public PscVerificationLinks getLinks() {
        return links;
    }

    public PscVerificationData getData() {
        return data;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final PscVerification that)) return false;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getCreatedAt(),
            that.getCreatedAt()) && Objects.equals(getUpdatedAt(),
            that.getUpdatedAt()) && Objects.equals(getLinks(), that.getLinks()) && Objects.equals(
            getData(), that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCreatedAt(), getUpdatedAt(), getLinks(), getData());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PscVerification.class.getSimpleName() + "[", "]").add(
                "id='" + getId() + "'")
            .add("createdAt=" + getCreatedAt())
            .add("updatedAt=" + getUpdatedAt())
            .add("links=" + getLinks())
            .add("data=" + getData())
            .toString();
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setLinks(final PscVerificationLinks links) {
        this.links = links;
    }

    public void setData(final PscVerificationData data) {
        this.data = data;
    }

    @Override
    public void touch(Instant instant) {
        this.updatedAt = instant;
    }

    /**
     * {@code PscVerification} builder static inner class.
     */
    public static final class Builder {
        private String id;
        private Instant createdAt;
        private Instant updatedAt;
        private PscVerificationLinks links;
        private PscVerificationData data;

        private Builder() {
        }

        /**
         * Sets the {@code id} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param id the {@code id} to set
         * @return a reference to this Builder
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the {@code createdAt} and returns a reference to this Builder so that the methods
         * can be chained together.
         *
         * @param createdAt the {@code createdAt} to set
         * @return a reference to this Builder
         */
        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * Sets the {@code updatedAt} and returns a reference to this Builder so that the methods
         * can be chained together.
         *
         * @param updatedAt the {@code updatedAt} to set
         * @return a reference to this Builder
         */
        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * Sets the {@code links} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param links the {@code links} to set
         * @return a reference to this Builder
         */
        public Builder links(PscVerificationLinks links) {
            this.links = links;
            return this;
        }

        /**
         * Sets the {@code data} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param data the {@code data} to set
         * @return a reference to this Builder
         */
        public Builder data(PscVerificationData data) {
            this.data = data;
            return this;
        }

        /**
         * Returns a {@code PscVerification} built from the parameters previously set.
         *
         * @return a {@code PscVerification} built with parameters of this
         * {@code PscVerification.Builder}
         */
        public PscVerification build() {
            return new PscVerification(this);
        }
    }
}
