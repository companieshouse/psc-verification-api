package uk.gov.companieshouse.pscverificationapi.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.patch.model.PatchResult;
import uk.gov.companieshouse.pscverificationapi.config.PatchServiceProperties;
import uk.gov.companieshouse.pscverificationapi.exception.MergePatchException;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.provider.PscVerificationFilingProvider;
import uk.gov.companieshouse.pscverificationapi.repository.PscVerificationRepository;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationPatchValidator;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;

@Service
public class PscVerificationServiceImpl implements PscVerificationService {
    private final PscVerificationRepository repository;
    private final PatchServiceProperties patchServiceProperties;
    private final PscVerificationFilingProvider pscVerificationFilingProvider;
    private final PscVerificationFilingMergeProcessor mergeProcessor;
    private final PscVerificationFilingPostMergeProcessor postMergeProcessor;
    private final PscVerificationPatchValidator pscVerificationPatchValidator;

    @Autowired
    public PscVerificationServiceImpl(PscVerificationRepository repository,
                                      PatchServiceProperties patchServiceProperties,
                                      PscVerificationFilingProvider pscVerificationFilingProvider,
                                      PscVerificationFilingMergeProcessor mergeProcessor,
                                      PscVerificationFilingPostMergeProcessor postMergeProcessor,
                                      PscVerificationPatchValidator pscVerificationPatchValidator) {
        this.repository = repository;
        this.patchServiceProperties = patchServiceProperties;
        this.pscVerificationFilingProvider = pscVerificationFilingProvider;
        this.mergeProcessor = mergeProcessor;
        this.postMergeProcessor = postMergeProcessor;
        this.pscVerificationPatchValidator = pscVerificationPatchValidator;
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
    public PatchResult patch(final String filingId, final Map<String, Object> patchMap) {
        final PatchResult patchResult;

        try {
            patchResult = patchEntity(filingId, pscVerificationFilingProvider, patchMap,
                    mergeProcessor, postMergeProcessor, pscVerificationPatchValidator);
        } catch (final IOException e) {
            throw new MergePatchException(e);
        }

        return patchResult;
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

    @Override
    public int save(PscVerification filing, String version) {
        repository.save(PscVerification.newBuilder(filing).build());
        return 1;
    }

    @Override
    public int getMaxRetries() {
        return patchServiceProperties.getMaxRetries();
    }
}
