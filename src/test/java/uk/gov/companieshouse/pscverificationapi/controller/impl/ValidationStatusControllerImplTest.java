package uk.gov.companieshouse.pscverificationapi.controller.impl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.pscverificationapi.controller.impl.ValidationStatusControllerImpl.TRANSACTION_NOT_SUPPORTED_ERROR;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.api.model.common.ResourceLinks;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.mapper.ErrorMapper;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@ExtendWith(MockitoExtension.class)
class ValidationStatusControllerImplTest {

    private static final String TRANS_ID = "117524-754816-491724";
    private static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String SELF_FRAGMENT =
            "/transactions/" + TRANS_ID + "/persons-with-significant-control-verification/";

    @Mock
    private PscVerificationService pscVerificationService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Logger logger;
    @Mock
    private ErrorMapper errorMapper;
    @Mock
    private Transaction transaction;

    private ValidationStatusControllerImpl testController;


    @BeforeEach
    void setUp() {
        testController = new ValidationStatusControllerImpl(pscVerificationService, errorMapper, true, logger);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
    }

    @Test
    void validateWhenClosableFlagFalse() {
        testController = new ValidationStatusControllerImpl(pscVerificationService, errorMapper, false, logger);
        final var filing = PscVerification.newBuilder().build();
        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.of(filing));

        final var response = testController.validate(TRANS_ID, FILING_ID, transaction, request);

        assertThat(response.isValid(), is(false));
        assertThat(response.getValidationStatusError(), is(arrayWithSize(1)));
        assertThat(response.getValidationStatusError()[0].getError(), is(TRANSACTION_NOT_SUPPORTED_ERROR));
    }

    @Test
    void validateWhenFilingNotFound() {
        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.empty());

        final var filingResourceNotFoundException = assertThrows(FilingResourceNotFoundException.class,
                () -> testController.validate(TRANS_ID, FILING_ID, transaction, request));

        assertThat(filingResourceNotFoundException.getMessage(), containsString(FILING_ID));
    }

    @Test
    void validateWhenFilingValid() {
        final var self = UriComponentsBuilder.fromUriString(SELF_FRAGMENT).pathSegment(FILING_ID).build().toUri();
        final var links = ResourceLinks.newBuilder().build();
        final var filing = PscVerification.newBuilder().links(links).build();

        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.of(filing));
        when(errorMapper.map(anyList())).thenReturn(new ValidationStatusError[0]);

        final var response = testController.validate(TRANS_ID, FILING_ID, transaction, request);

        assertThat(response.isValid(), is(true));
        assertThat(response.getValidationStatusError(), is(emptyArray()));

    }

}