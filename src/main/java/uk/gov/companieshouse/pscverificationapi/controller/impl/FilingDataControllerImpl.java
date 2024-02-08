package uk.gov.companieshouse.pscverificationapi.controller.impl;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.controller.FilingDataController;
import uk.gov.companieshouse.pscverificationapi.helper.LogMapHelper;
import uk.gov.companieshouse.pscverificationapi.service.FilingDataService;

@RestController
@RequestMapping("/private/transactions/{transactionId}/persons-with-significant-control-verification/")
public class FilingDataControllerImpl implements FilingDataController {
    private final FilingDataService filingDataService;
    private final Logger logger;

    public FilingDataControllerImpl(final FilingDataService filingDataService, final Logger logger) {
        this.filingDataService = filingDataService;
        this.logger = logger;
    }

    /**
     * Controller endpoint: retrieve Filing Data. Returns a list containing a single resource;
     * Future capability to return multiple resources if a Transaction contains multiple PSC
     * Filings.
     *
     * @param transId        the transaction ID
     * @param filingResource the Filing Resource ID
     * @param transaction    the Transaction
     * @param request        the servlet request
     * @return List of FilingApi resources
     */
    @Override
    @GetMapping(value = "/{filingResourceId}/filings", produces = {"application/json"})
    public List<FilingApi> getFilingsData(@PathVariable("transactionId") final String transId,
                                          @PathVariable("filingResourceId") final String filingResource,
                                          @RequestAttribute(required = false, name = "transaction")
                                          Transaction transaction, final HttpServletRequest request) {

        final var logMap = LogMapHelper.createLogMap(transId, filingResource);

        logger.debugRequest(request,
                "GET /private/transactions/{transactionId}/persons-with-significant-control-verification" +
                        "/{filingId}/filings", logMap);

        final var filingApi = filingDataService.generatePscVerification(filingResource, transaction);

        logMap.put("psc verification:", filingApi);
        logger.infoContext(transId, "psc verification data", logMap);

        return List.of(filingApi);
    }

}