package uk.gov.companieshouse.pscverificationapi.repository;

import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;

public class PscVerificationRepositoryImpl implements PscVerificationRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public PscVerificationRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<PscVerification> findByNotificationId(String notificationId) {

        return mongoTemplate.find(
            query(Criteria.where("data.psc_notification_id").is(notificationId)), PscVerification.class);
    }
}