package uk.gov.companieshouse.pscverificationapi.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;

@ExtendWith(MockitoExtension.class)
class PscVerificationFilingPostMergeProcessorTest {
    private static final Instant FIRST_INSTANT = Instant.parse("2024-10-15T09:44:08.108Z");

    private PscVerificationFilingPostMergeProcessor testProcessor;

    @Mock
    private Clock clock;
    @Mock
    private PscVerification filing;

    @BeforeEach
    void setUp() {
        testProcessor = new PscVerificationFilingPostMergeProcessor(clock);
    }

    @Test
    void onMerge() {
        when(clock.instant()).thenReturn(FIRST_INSTANT);

        testProcessor.onMerge(filing);

        verify(filing).touch(FIRST_INSTANT);

    }

}