package uk.gov.companieshouse.pscverificationapi.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;
import java.util.StringJoiner;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.gov.companieshouse.api.model.common.ResourceLinks;
import uk.gov.companieshouse.api.model.pscverification.InternalData;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;

/**
 * Entity representing a PSC verification filing.
 * <p>
 * Stores PSC verification data, internal data, resource links, and timestamps for persistence.
 * Includes a builder for constructing immutable instances.
 * </p>
 */
@Document(collection = "psc_verification")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PscVerification implements Touchable {
    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant updatedAt;
    @JsonMerge
    @JsonProperty(access= JsonProperty.Access.READ_ONLY)
    private ResourceLinks links;
    @JsonMerge
    @JsonProperty("data")
    private PscVerificationData data;
    // No @JsonMerge: this property MUST NOT be modifiable by PATCH requests
    @JsonProperty("internal_data")
    private InternalData internalData;

    public PscVerification() {
        // required by Spring Data
    }

    private PscVerification(final Builder builder) {
        setId(builder.id);
        setCreatedAt(builder.createdAt);
        setUpdatedAt(builder.updatedAt);
        setLinks(builder.links);
        setData(builder.data);
        setInternalData(builder.internalData);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final PscVerification copy) {
        final Builder builder = new Builder();
        builder.id = copy.getId();
        builder.createdAt = copy.getCreatedAt();
        builder.updatedAt = copy.getUpdatedAt();
        builder.links = copy.getLinks();
        builder.data = copy.getData();
        builder.internalData = copy.getInternalData();
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

    public ResourceLinks getLinks() {
        return links;
    }

    public PscVerificationData getData() {
        return data;
    }

    public InternalData getInternalData() {
        return internalData;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final PscVerification that)) return false;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getCreatedAt(),
            that.getCreatedAt()) && Objects.equals(getUpdatedAt(),
            that.getUpdatedAt()) && Objects.equals(getLinks(), that.getLinks()) && Objects.equals(
            getData(), that.getData()) && Objects.equals(getInternalData(), that.getInternalData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCreatedAt(), getUpdatedAt(), getLinks(), getData(), getInternalData());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PscVerification.class.getSimpleName() + "[", "]").add(
                "id='" + getId() + "'")
            .add("createdAt=" + getCreatedAt())
            .add("updatedAt=" + getUpdatedAt())
            .add("links=" + getLinks())
            .add("data=" + getData())
            .add("internalData=" + getInternalData())
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

    public void setLinks(final ResourceLinks links) {
        this.links = links;
    }

    public void setData(final PscVerificationData data) {
        this.data = data;
    }

    public void setInternalData(final InternalData internalData) {
        this.internalData = internalData;
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
        private ResourceLinks links;
        private PscVerificationData data;
        private InternalData internalData;

        private Builder() {
        }

        /**
         * Sets the {@code id} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param id the {@code id} to set
         * @return a reference to this Builder
         */
        public Builder id(final String id) {
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
        public Builder createdAt(final Instant createdAt) {
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
        public Builder updatedAt(final Instant updatedAt) {
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
        public Builder links(final ResourceLinks links) {
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
        public Builder data(final PscVerificationData data) {
            this.data = data;
            return this;
        }

        /**
         * Sets the {@code internalData} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param internalData the {@code internalData} to set
         * @return a reference to this Builder
         */
        public Builder internalData(final InternalData internalData) {
            this.internalData = internalData;
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
