package uk.gov.companieshouse.pscverificationapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.pscverificationapi.exception.NotImplementedException;

/**
 * Interface for getting the validation status of a PSC filing resource.
 * <p>
 * This controller manages HTTP requests for creating, updating, and retrieving
 * PSC verification filings.
 * </p>
 */
public interface ValidationStatusController {

    /**
     * Controller endpoint: Validate the filing resource and return the status.
     *
     * @param transId        the transaction ID
     * @param filingResource the Filing Resource ID
     * @param transaction    the Transaction
     * @param request        the servlet request
     * @return ValidationStatusResponse object
     */
    @GetMapping(value = "/{filingResourceId}/validation_status", produces = {"application/json"})
    default ValidationStatusResponse validate(@PathVariable("transactionId") final String transId,
                                              @PathVariable("filingResourceId") String filingResource,
                                              @RequestAttribute("transaction") Transaction transaction,
                                              final HttpServletRequest request) {

        throw new NotImplementedException();
    }
}
