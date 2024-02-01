package uk.gov.companieshouse.pscverificationapi.service.impl;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.patch.service.PostMergeProcessor;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;

@Component
public class PscVerificationFilingPostMergeProcessor implements PostMergeProcessor<PscVerification> {
    private final Clock clock;

    @Autowired
    public PscVerificationFilingPostMergeProcessor(final Clock clock) {
        this.clock = clock;
    }

    @Override
    public void onMerge(final PscVerification filing) {
        filing.touch(clock.instant());
    }
}
