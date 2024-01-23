package uk.gov.companieshouse.pscverificationapi.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.repository.PscVerificationRepository;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;

@Service
public class PscVerificationServiceImpl implements PscVerificationService {
    private final PscVerificationRepository repository;

    public PscVerificationServiceImpl(final PscVerificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Store a PscVerification entity in persistence layer.
     *
     * @param filing the PscVerification entity to store
     * @return the stored entity
     */
    @Override
    public PscVerification save(final PscVerification filing) {
        return repository.save(filing);
    }

    /**
     * Retrieve a stored PSCFiling entity by Filing ID.
     *
     * @param filingId the Filing ID
     * @return the stored entity if found
     */
    @Override
    public Optional<PscVerification> get(final String filingId) {
        return repository.findById(filingId);
    }

    @Override
    public boolean requestMatchesResourceSelf(final HttpServletRequest request,
        final PscVerification filing) {
        final var selfLinkUri = filing.getLinks().getSelf();
        final URI requestUri;

        try {
            requestUri = new URI(request.getRequestURI());
        } catch (final URISyntaxException e) {
            return false;
        }

        return selfLinkUri.equals(requestUri.normalize().toString());
    }

}
