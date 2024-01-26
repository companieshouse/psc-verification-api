package uk.gov.companieshouse.pscverificationapi.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.exception.NotImplementedException;


class ValidationStatusControllerTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private Transaction transaction;

    @Test
    void validate() {
        var testController = new ValidationStatusController() {
        };

        assertThrows(NotImplementedException.class,
                () -> testController.validate("trans-id", "filing-id", transaction, request));
    }

}