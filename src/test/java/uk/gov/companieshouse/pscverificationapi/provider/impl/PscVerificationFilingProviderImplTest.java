package uk.gov.companieshouse.pscverificationapi.provider.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.patch.model.EntityRetrievalResult;
import uk.gov.companieshouse.pscverificationapi.error.RetrievalFailureReason;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.provider.PscVerificationFilingProvider;
import uk.gov.companieshouse.pscverificationapi.repository.PscVerificationRepository;

@ExtendWith(MockitoExtension.class)
class PscVerificationFilingProviderImplTest {
    private static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private PscVerificationFilingProvider testProvider;

    @Mock
    private PscVerificationRepository repository;
    @Mock
    private Logger logger;

    private PscVerification filing;

    @BeforeEach
    void setUp() {
        testProvider = new PscVerificationFilingProviderImpl(repository, logger);
        filing = PscVerification.newBuilder().id(FILING_ID).build();
    }

    @Test
    void provide() {
        final var expected = new EntityRetrievalResult<>("TBC", filing);
        when(repository.findById(FILING_ID)).thenReturn(Optional.of(filing));

        final var result = testProvider.provide(FILING_ID);

        assertThat(result.isSuccess(), is(true));
        assertThat(result, samePropertyValuesAs(expected));
    }

    @Test
    void provideWhenWrongFilingId() {
        final var differentFiling = PscVerification.newBuilder(filing).id(FILING_ID + "x").build();
        when(repository.findById(FILING_ID)).thenReturn(Optional.of(differentFiling));

        final var result = testProvider.provide(FILING_ID);

        assertThat(result.isSuccess(), is(false));
        assertThat(result.getFailureReason(), is(RetrievalFailureReason.FILING_NOT_FOUND));
    }

}