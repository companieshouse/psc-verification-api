package uk.gov.companieshouse.pscverificationapi.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.common.ResourceLinks;
import uk.gov.companieshouse.patch.model.EntityRetrievalResult;
import uk.gov.companieshouse.patch.model.ValidationResult;
import uk.gov.companieshouse.pscverificationapi.config.PatchServiceProperties;
import uk.gov.companieshouse.pscverificationapi.exception.MergePatchException;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.provider.PscVerificationFilingProvider;
import uk.gov.companieshouse.pscverificationapi.repository.PscVerificationRepository;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationPatchValidator;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;

@ExtendWith(MockitoExtension.class)
class PscVerificationServiceImplTest {
    public static final String FILING_ID = "6332aa6ed28ad2333c3a520a";

    @Spy
    private PscVerificationService testService;
    @Mock
    private PscVerificationRepository repository;
    @Mock
    private PatchServiceProperties patchServiceProperties;
    @Mock
    private PscVerificationFilingProvider provider;
    @Mock
    private PscVerificationFilingMergeProcessor mergeProcessor;
    @Mock
    private PscVerificationFilingPostMergeProcessor postMergeProcessor;
    @Mock
    private PscVerificationPatchValidator patchValidator;
    @Mock
    private HttpServletRequest request;

    private PscVerification filing;

    @BeforeEach
    void setUp() {
        testService = new PscVerificationServiceImpl(repository, patchServiceProperties, provider,
            mergeProcessor, postMergeProcessor, patchValidator);
        filing = PscVerification.newBuilder().build();
    }

    @Test
    void save() {
        testService.save(filing, "TBC");

        verify(repository).save(filing);
    }

    @Test
    void get() {
        testService.get(FILING_ID);

        verify(repository).findById(FILING_ID);
    }

    @Test
    void patch() throws IOException {
        when(testService.getMaxRetries()).thenReturn(1);
        final EntityRetrievalResult<PscVerification> retrievalResult = new EntityRetrievalResult<>(
            "TBC", filing);
        when(provider.provide(FILING_ID)).thenReturn(retrievalResult);
        when(mergeProcessor.mergeEntity(filing, Collections.emptyMap())).thenReturn(filing);
        when(patchValidator.validate(filing)).thenReturn(new ValidationResult());

        final var result = testService.patch(FILING_ID, Collections.emptyMap());

        verify(postMergeProcessor).onMerge(filing);
        verify(repository).save(filing);
        assertThat(result.isSuccess(), is(true));

    }

    @Test
    void patchWhenExceptionThrown() throws IOException {
        final Map<String, Object> patchMap = Collections.emptyMap();

        when(testService.getMaxRetries()).thenReturn(1);
        final EntityRetrievalResult<PscVerification> retrievalResult = new EntityRetrievalResult<>(
            "TBC", filing);
        when(provider.provide(FILING_ID)).thenReturn(retrievalResult);
        when(mergeProcessor.mergeEntity(filing, Collections.emptyMap())).thenThrow(
            new IOException("ioe"));

        final var exception = assertThrows(MergePatchException.class,
            () -> testService.patch(FILING_ID, patchMap));

        verifyNoInteractions(postMergeProcessor, repository);
        assertThat(exception.getCause().getMessage(), is("ioe"));

    }

    @Test
    void requestMatchesResourceSelf() {
        final var self = URI.create("self");
        final var filingWithLinks = PscVerification.newBuilder(filing)
            .links(ResourceLinks.newBuilder().self(self).build())
            .build();

        when(request.getRequestURI()).thenReturn(self.toString());

        assertThat(testService.requestMatchesResourceSelf(request, filingWithLinks), is(true));
    }

    @Test
    void requestMatchesResourceSelfWhenUriInvalid() {
        final var self = URI.create("self");
        final var filingWithLinks = PscVerification.newBuilder(filing)
            .links(ResourceLinks.newBuilder().self(self).build())
            .build();

        when(request.getRequestURI()).thenReturn(":::");

        assertThat(testService.requestMatchesResourceSelf(request, filingWithLinks), is(false));
    }


    @Test
    void getMaxRetries() {

        when(patchServiceProperties.getMaxRetries()).thenReturn(123);

        assertThat(testService.getMaxRetries(), is(123));

    }
}