package uk.gov.companieshouse.pscverificationapi.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.pscverificationapi.model.PscVerificationDataForUpdating;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.model.mapper.PscVerificationMapper;

@ExtendWith(MockitoExtension.class)
class PscVerificationFilingMergeProcessorTest {
    private PscVerificationFilingMergeProcessor testProcessor;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ObjectMapper patchObjectMapper;
    @Mock
    private PscVerificationMapper dataMapper;

    @BeforeEach
    void setUp() {
        testProcessor = new PscVerificationFilingMergeProcessor(patchObjectMapper, dataMapper);
    }

    @Test
    void mergeEntity() throws IOException {

        final Map<String, Object> patchMap = Map.of("company_number", "TWO");
        final var targetData = PscVerificationData.newBuilder().companyNumber("ONE").build();
        final var target = PscVerification.newBuilder().data(targetData).build();
        final var one = new PscVerificationDataForUpdating("ONE", null, null, null);
        final var two = new PscVerificationDataForUpdating("TWO", null, null, null);

        when(dataMapper.toForUpdating(targetData)).thenReturn(one);
        when(patchObjectMapper.updateValue(one, patchMap)).thenReturn(two);
        when(dataMapper.fromForUpdating(two)).thenReturn(
            PscVerificationData.newBuilder().companyNumber("TWO").build());

        final var mergedEntity = testProcessor.mergeEntity(target, patchMap);

        assertThat(mergedEntity.getData().companyNumber(), is("TWO"));
        verify(patchObjectMapper).updateValue(one, patchMap);
    }

}