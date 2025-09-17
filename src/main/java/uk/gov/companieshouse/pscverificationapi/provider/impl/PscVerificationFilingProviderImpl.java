package uk.gov.companieshouse.pscverificationapi.provider.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.patch.model.EntityRetrievalResult;
import uk.gov.companieshouse.pscverificationapi.error.RetrievalFailureReason;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.provider.PscVerificationFilingProvider;
import uk.gov.companieshouse.pscverificationapi.repository.PscVerificationRepository;

/**
 * Provider implementation for the {@link PscVerificationFilingProvider} interface.
 */
@Component
public class PscVerificationFilingProviderImpl implements PscVerificationFilingProvider {
    private final Logger logger;
    private final PscVerificationRepository pscVerificationRepository;

    @Autowired
    public PscVerificationFilingProviderImpl(final PscVerificationRepository pscVerificationRepository, final Logger logger) {
        this.pscVerificationRepository = pscVerificationRepository;
        this.logger = logger;
    }

    @Override
    public EntityRetrievalResult<PscVerification> provide(final String filingId) {
        final var pscVerification = pscVerificationRepository.findById(filingId);
        logger.debug("providing filing resource: " + filingId);

        return pscVerification
                .filter(f -> f.getId().equals(filingId))
                .map(f -> new EntityRetrievalResult<>("TBC", f))
                .orElseGet(
                        () -> new EntityRetrievalResult<>(RetrievalFailureReason.FILING_NOT_FOUND));
    }

}
