package uk.gov.companieshouse.pscverificationapi.repository;

import java.util.List;

import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;

public interface PscVerificationRepositoryCustom {

    /**
     * Get a transaction based on notificationId .
     *
     * @param notificationId .
     * @return transaction .
     */
    List<PscVerification> findByNotificationId(String notificationId);

}
