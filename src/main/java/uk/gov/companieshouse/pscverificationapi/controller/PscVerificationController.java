package uk.gov.companieshouse.pscverificationapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.exception.NotImplementedException;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;

public interface PscVerificationController {
    default ResponseEntity<PscVerificationApi> createPscVerification(
        @PathVariable("transactionId") final String transId,
        @RequestAttribute(required = false, name = "transaction") final Transaction transaction,
        @RequestBody @Valid @NotNull final PscVerificationData data, final BindingResult result,
        final HttpServletRequest request) {
        throw new NotImplementedException();
    }

    /**
     * Retrieve PSC verification submission.
     *
     * @param transId        the Transaction ID
     * @param filingResourceId the PSC Filing ID
     * @throws NotImplementedException implementing classes must perform work
     */
    @GetMapping
    default ResponseEntity<PscVerification> getPscVerification(
            @PathVariable("transactionId") final String transId,
            @PathVariable("filingResourceId") final String filingResourceId,
            final HttpServletRequest request) {
        throw new NotImplementedException();
    }
}
