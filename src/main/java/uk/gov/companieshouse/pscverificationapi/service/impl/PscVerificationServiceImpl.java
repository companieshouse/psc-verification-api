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

/**
 * Service interface for managing PSC verification filings.
 * <p>
 * Provides methods for storing, retrieving, updating, and matching {@link PscVerification} entities.
 * Implements {@link PscVerificationService}.
 * </p>
 */
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

    @Override
    public PscVerification save(final PscVerification filing) {
        return repository.save(filing);
    }

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
            throw new MergePatchException(e.getMessage(), e);
        }

        return patchResult;
    }

    @Override
    public boolean requestMatchesResourceSelf(final HttpServletRequest request,
        final PscVerification filing) {
        final var selfLinkUri = filing.getLinks().self();
        final URI requestUri;

        try {
            requestUri = new URI(request.getRequestURI());
        } catch (final URISyntaxException e) {
            return false;
        }

        return selfLinkUri.equals(requestUri.normalize());
    }

    @Override
    public int save(PscVerification filing, String version) {
        repository.save(filing);
        return 1;
    }

    @Override
    public int getMaxRetries() {
        return patchServiceProperties.getMaxRetries();
    }
}
