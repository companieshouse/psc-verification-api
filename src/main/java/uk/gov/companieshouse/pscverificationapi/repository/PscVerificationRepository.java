package uk.gov.companieshouse.pscverificationapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;

public interface PscVerificationRepository extends MongoRepository<PscVerification, String> {
}
