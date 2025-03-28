package uk.gov.companieshouse.pscverificationapi.controller.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.service.FilingDataService;

@ExtendWith(MockitoExtension.class)
class FilingDataControllerImplTest extends BaseControllerIT {
    @Mock
    private FilingDataService filingDataService;
    @Mock
    private Logger logger;
    @Mock
    private HttpServletRequest request;

    private FilingDataControllerImpl testController;
    private Transaction filingsTransaction;

    @BeforeEach
    void setUp() {
        testController =
                new FilingDataControllerImpl(filingDataService, logger);
        filingsTransaction = new Transaction();
        filingsTransaction.setId(TRANS_ID);
        filingsTransaction.setCompanyNumber(COMPANY_NUMBER);
    }

    @Test
    void getFilingsData() {
        var filingApi = new FilingApi();
        when(filingDataService.generateFilingApi(FILING_ID, filingsTransaction)).thenReturn(filingApi);

        final var filingsList = testController.getFilingsData(TRANS_ID, FILING_ID, filingsTransaction, request);

        assertThat(filingsList, Matchers.contains(filingApi));
    }

    @Test
    void getFilingsDataWhenNotFound() {
        when(filingDataService.generateFilingApi(FILING_ID, filingsTransaction)).thenThrow(
                new FilingResourceNotFoundException("Test Resource not found"));

        final var exception = assertThrows(FilingResourceNotFoundException.class,
                () -> testController.getFilingsData(TRANS_ID, FILING_ID, filingsTransaction, request));
        assertThat(exception.getMessage(), is("Test Resource not found"));
    }
}