package uk.gov.companieshouse.pscverificationapi.pscverification;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationLinks;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;

@RestController
@RequestMapping("/transactions/{transactionId}/persons-with-significant-control-verification")
public class PscVerificationControllerImpl implements PscVerificationController {
    private final Logger logger;

    public PscVerificationControllerImpl(final Logger logger) {
        this.logger = logger;
    }


    @Override
    //  @Transactional
    // TODO: delayed until Spring Data code is added
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PscVerificationApi> createPscVerification(
        @PathVariable("transactionId") final String transId,
        @RequestAttribute(required = false, name = "transaction") final Transaction transaction,
        @RequestBody @Valid @NotNull final PscVerificationData data, final BindingResult result,
        final HttpServletRequest request) {

        final var placeholderId = UUID.randomUUID();
        final var self = "/transactions/" + transId + "/persons-with-significant-control"
            + "-verification/" + placeholderId;
        final var responseBody = new PscVerificationApi();
        final var links = new PscVerificationLinks();

        final var location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(placeholderId)
            .toUri();

        links.setSelf(self);
        links.setValidationStatus(self + "/validation_status");
        responseBody.setLinks(links);

        return ResponseEntity.created(location).body(responseBody);
    }
}
