package uk.gov.companieshouse.pscverificationapi.pscverification;

import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.exception.NotImplementedException;

@ExtendWith(MockitoExtension.class)
class PscVerificationControllerTest {
    private final PscVerificationController testController = new PscVerificationController() { };

    @Mock
    private PscVerificationData data;
    @Mock
    private Transaction transaction;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private HttpServletRequest request;

    @Test
    void createPscVerification() {
        assertThrows(NotImplementedException.class,
            () -> testController.createPscVerification("trans-id", transaction, data, bindingResult,
                request));
    }
}