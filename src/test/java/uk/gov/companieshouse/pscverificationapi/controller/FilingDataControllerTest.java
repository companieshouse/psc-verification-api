package uk.gov.companieshouse.pscverificationapi.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.exception.NotImplementedException;

class FilingDataControllerTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private Transaction transaction;

    @Test
    void getFilingsData() {

        var testController = new FilingDataController(){};

        assertThrows(NotImplementedException.class,
            () -> testController.getFilingsData("trans-id",
                "filing-id", transaction, request));
    }
}