package uk.gov.companieshouse.pscverificationapi.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import uk.gov.companieshouse.patch.model.PatchResult;
import uk.gov.companieshouse.patch.service.PatchService;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;

public interface PscVerificationService extends PatchService<PscVerification> {
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

    /**
     * Update a PSCVerification entity by Filing ID.
     *
     * @param filingId   the Filing ID
     * @param patchMap   a list of parameters to include in the patch
     * @return the patch result
     */
    PatchResult patch(final String filingId, final Map<String, Object> patchMap);

    boolean requestMatchesResourceSelf(HttpServletRequest request, PscVerification filing);
}
