package uk.gov.companieshouse.pscverificationapi.model.entity;

import java.time.Instant;

@FunctionalInterface
public interface Touchable {
    void touch(Instant instant);
}
