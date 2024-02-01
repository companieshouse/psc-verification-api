package uk.gov.companieshouse.pscverificationapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.patch.service.MergeProcessor;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;

@Component
public class PscVerificationFilingMergeProcessor implements MergeProcessor<PscVerification> {

    private final ObjectMapper patchObjectMapper;

    @Autowired
    public PscVerificationFilingMergeProcessor(
            @Qualifier("patchObjectMapper") final ObjectMapper patchObjectMapper) {
        this.patchObjectMapper = patchObjectMapper;
    }

    @Override
    public PscVerification mergeEntity(PscVerification target, Map<String, Object> patchMap) throws IOException {
//        final var patchObjectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
//                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
//                .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        final var json = patchObjectMapper.writeValueAsString(patchMap);

        final var patched = patchObjectMapper.readerForUpdating(target).readValue(json);
        return (PscVerification) patched;
    }
}
