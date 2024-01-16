package uk.gov.companieshouse.pscverificationapi.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;

public interface PscVerificationService {
    /**
     * Store a PscVerification entity in persistence layer.
     *
     * @param filing        the PscVerification entity to store
     * @return the stored entity
     */
    PscVerification save(PscVerification filing);

    /**
     * Retrieve a stored PscVerification entity by Filing ID.
     *
     * @param filingId   the Filing ID
     * @return the stored entity if found
     */
    Optional<PscVerification> get(String filingId);

    boolean requestMatchesResourceSelf(HttpServletRequest request, PscVerification filing);
}
