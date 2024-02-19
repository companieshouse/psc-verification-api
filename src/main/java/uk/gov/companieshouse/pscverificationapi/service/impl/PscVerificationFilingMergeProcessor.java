package uk.gov.companieshouse.pscverificationapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.patch.service.MergeProcessor;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.model.mapper.PscVerificationMapper;

@Component
public class PscVerificationFilingMergeProcessor implements MergeProcessor<PscVerification> {

    private final ObjectMapper patchObjectMapper;
    private final PscVerificationMapper dataMapper;

    @Autowired
    public PscVerificationFilingMergeProcessor(
        @Qualifier("patchObjectMapper") final ObjectMapper patchObjectMapper,
        PscVerificationMapper dataMapper) {
        this.patchObjectMapper = patchObjectMapper;
        this.dataMapper = dataMapper;
    }

    @Override
    public PscVerification mergeEntity(final PscVerification target,
        final Map<String, Object> patchMap) throws IOException {
        final var targetDto = target.getData();
        final var targetPojo = dataMapper.toForUpdating(targetDto);
        final var mergedPojo = patchObjectMapper.updateValue(targetPojo, patchMap);
        final var mergedDto = dataMapper.fromForUpdating(mergedPojo);

        return PscVerification.newBuilder(target).data(mergedDto).build();
    }
}
