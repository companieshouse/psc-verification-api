package uk.gov.companieshouse.pscverificationapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;

/**
 * MongoDB repository for PSC verification entities.
 */
public interface PscVerificationRepository extends MongoRepository<PscVerification, String> {
}
