package uk.gov.companieshouse.pscverificationapi.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.exception.NotImplementedException;


@ExtendWith(MockitoExtension.class)
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