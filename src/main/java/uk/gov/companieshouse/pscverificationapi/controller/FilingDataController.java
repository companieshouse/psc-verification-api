package uk.gov.companieshouse.pscverificationapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.exception.NotImplementedException;

/**
 * Interface for handling filing data operations.
 * <p>
 * This controller is responsible for retrieving filing data associated with
 * a specific transaction and PSC filing resource. 
 * </p>
 */
public interface FilingDataController {

    /**
     * Controller endpoint: retrieve Filing Data.
     *
     * @param transId        the transaction ID
     * @param filingResource the Filing Resource ID
     * @param transaction    the Transaction
     * @param request        the servlet request
     * @throws NotImplementedException implementing classes must perform work
     */
    @GetMapping
    default List<FilingApi> getFilingsData(@PathVariable("transactionId") final String transId,
                                           @PathVariable("filingResource") String filingResource,
                                           @RequestAttribute("transaction") Transaction transaction,
                                           HttpServletRequest request) {
        throw new NotImplementedException();
    }
}
