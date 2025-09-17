package uk.gov.companieshouse.pscverificationapi.model.entity;

import java.time.Instant;

/**
 * Functional interface for updating an entity's timestamp.
 */
@FunctionalInterface
public interface Touchable {
    void touch(Instant instant);
}
