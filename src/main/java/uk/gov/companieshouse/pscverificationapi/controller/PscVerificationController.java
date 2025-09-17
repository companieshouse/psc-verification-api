package uk.gov.companieshouse.pscverificationapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.exception.NotImplementedException;

/**
 * Interface for handling PSC verification operations.
 * <p>
 * This controller manages HTTP requests for creating, updating, and retrieving
 * PSC verification filings.
 * </p>
 */
public interface PscVerificationController {

    /**
     * Create an PSC Verification Statement.
     * @param transId       the transaction ID
     * @param transaction   the Transaction
     * @param data          the request body payload DTO
     * @param result        the MVC binding result (with any validation errors)
     * @param request       the servlet request
     * @throws NotImplementedException implementing classes must perform work
     */
    default ResponseEntity<PscVerificationApi> createPscVerification(
        @PathVariable("transactionId") final String transId,
        @RequestAttribute(required = false, name = "transaction") final Transaction transaction,
        @RequestBody @Valid @NotNull final PscVerificationData data, final BindingResult result,
        final HttpServletRequest request) {
        throw new NotImplementedException();
    }

    /**
     * Retrieve a PSC Verification submission by filing resource ID.
     *
     * @param transId        the Transaction ID
     * @param filingResourceId the PSC Filing ID
     * @param request           the servlet request
     * @throws NotImplementedException implementing classes must perform work
     */
    @GetMapping
    default ResponseEntity<PscVerificationApi> getPscVerification(
            @PathVariable("transactionId") final String transId,
            @PathVariable("filingResourceId") final String filingResourceId,
            final HttpServletRequest request) {
        throw new NotImplementedException();
    }

    /** 
    * Update a PSC Verification Statement filing resource by applying a JSON merge-patch.
    *
    * @param transId        the transaction ID
    * @param filingResource the PSC Verification Filing Resource ID
    * @param mergePatch     details of the merge-patch to apply (RFC 7396)
    * @param request        the servlet request
    * @throws NotImplementedException implementing classes must perform work
    * @see <a href="https://www.rfc-editor.org/rfc/rfc7396">RFC7396</a>
    */
    @PatchMapping
    default ResponseEntity<PscVerificationApi> updatePscVerification(
            @PathVariable("transactionId") final String transId,
            @PathVariable("filingResource") String filingResource,
            @RequestBody final @NotNull Map<String, Object> mergePatch, final HttpServletRequest request) {
        throw new NotImplementedException();
    }
}
