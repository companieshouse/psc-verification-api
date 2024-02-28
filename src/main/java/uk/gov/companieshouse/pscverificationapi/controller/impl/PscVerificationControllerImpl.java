package uk.gov.companieshouse.pscverificationapi.controller.impl;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.bson.types.ObjectId;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.api.model.common.ResourceLinks;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.controller.PscVerificationController;
import uk.gov.companieshouse.pscverificationapi.exception.InvalidFilingException;
import uk.gov.companieshouse.pscverificationapi.helper.LogMapHelper;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.model.mapper.PscVerificationMapper;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;
import uk.gov.companieshouse.pscverificationapi.service.TransactionService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@RestController
@RequestMapping("/transactions/{transactionId}/persons-with-significant-control-verification")
public class PscVerificationControllerImpl implements PscVerificationController {
    public static final String VALIDATION_STATUS = "validation_status";

    private final TransactionService transactionService;
    private final PscVerificationService pscVerificationService;
    private final PscVerificationMapper filingMapper;
    private final Clock clock;
    private final Logger logger;

    public PscVerificationControllerImpl(final TransactionService transactionService,
        final PscVerificationService pscVerificationService,
        final PscVerificationMapper filingMapper, final Clock clock, final Logger logger) {
        this.transactionService = transactionService;
        this.pscVerificationService = pscVerificationService;
        this.filingMapper = filingMapper;
        this.clock = clock;
        this.logger = logger;
    }

    @Override
    @Transactional
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PscVerificationApi> createPscVerification(
        @PathVariable("transactionId") final String transId,
        @RequestAttribute(required = false, name = "transaction") final Transaction transaction,
        @RequestBody @Valid @NotNull final PscVerificationData data, final BindingResult result,
        final HttpServletRequest request) {
        final var logMap = LogMapHelper.createLogMap(transId);

        logger.debugRequest(request, "POST", logMap);

        Optional.ofNullable(result).ifPresent(PscVerificationControllerImpl::checkBindingErrors);

        final var requestTransaction = getTransaction(transId, transaction, logMap,
            getPassthroughHeader(request));

        final var entity = filingMapper.toEntity(data);
        final var savedEntity = saveFilingWithLinks(entity, transId, request, logMap);

        if (transaction != null) {
            updateTransactionResources(requestTransaction, savedEntity.getLinks());
        }

        final var response = filingMapper.toApi(savedEntity);

        return ResponseEntity.created(savedEntity.getLinks().self()).body(response);
    }

    /**
     * Retrieve PSC Verification submission.
     *
     * @param transId           the Transaction ID
     * @param filingResourceId  the PSC Filing ID
     */
    @Override
    @GetMapping(value = "/{filingResourceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PscVerificationApi> getPscVerification(
            @PathVariable("transactionId") final String transId,
            @PathVariable("filingResourceId") final String filingResourceId,
            final HttpServletRequest request) {

        final var pscVerification = pscVerificationService.get(filingResourceId)
                .filter(f -> pscVerificationService.requestMatchesResourceSelf(request,
                        f));

        return pscVerification.map(filingMapper::toApi).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound()
                        .build());
    }

    /**
     * Retrieves the transaction resource
     *
     * @param transId           the transaction ID.
     * @param transaction       the transaction resource.
     * @param logMap            a list of parameters to include in a log message
     * @param passthroughHeader the passthroughHeader, includes authorisation for transaction fetch
     */
    private Transaction getTransaction(final String transId, Transaction transaction,
        final Map<String, Object> logMap, final String passthroughHeader) {
        if (transaction == null) {
            transaction = transactionService.getTransaction(transId, passthroughHeader);
        }

        logger.infoContext(transId, "transaction found", logMap);
        return transaction;
    }

    private String getPassthroughHeader(final HttpServletRequest request) {
        return request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());
    }

    protected static void checkBindingErrors(final BindingResult bindingResult) {
        final var validationErrors = Optional.ofNullable(bindingResult)
            .map(Errors::getFieldErrors)
            .map(ArrayList::new)
            .orElseGet(ArrayList::new);

        if (!validationErrors.isEmpty()) {
            throw new InvalidFilingException(validationErrors);
        }
    }

    private PscVerification saveFilingWithLinks(final PscVerification entity, final String transId,
        final HttpServletRequest request, final Map<String, Object> logMap) {

        logger.debugContext(transId, "saving PSC Verification", logMap);

        final var now = clock.instant();
        final var entityWithCreatedUpdated = PscVerification.newBuilder(entity)
            .createdAt(now)
            .updatedAt(now)
            .build();
        final var saved = pscVerificationService.save(entityWithCreatedUpdated);
        final var links = buildLinks(request, saved);
        final var updatedWithLinks = PscVerification.newBuilder(saved).links(links).build();
        final var resaved = pscVerificationService.save(updatedWithLinks);

        logMap.put("filing_id", resaved.getId());
        logger.infoContext(transId, "Filing saved", logMap);

        return resaved;
    }

    private ResourceLinks buildLinks(final HttpServletRequest request,
        final PscVerification savedFiling) {
        final var objectId = new ObjectId(Objects.requireNonNull(savedFiling.getId()));
        final var selfUri = UriComponentsBuilder.fromUriString(request.getRequestURI())
            .pathSegment(objectId.toHexString())
            .build()
            .toUri();
        final var validateUri = UriComponentsBuilder.fromUriString(request.getRequestURI())
            .pathSegment(objectId.toHexString())
            .pathSegment(VALIDATION_STATUS)
            .build()
            .toUri();

        return ResourceLinks.newBuilder()
            .self(selfUri)
            .validationStatus(validateUri)
            .build();
    }

    private void updateTransactionResources(final Transaction transaction,
        final ResourceLinks links) {
        final var resourceMap = buildResourceMap(links);

        transaction.setResources(resourceMap);
        transactionService.updateTransaction(transaction);
    }

    private Map<String, Resource> buildResourceMap(final ResourceLinks links) {
        final Map<String, Resource> resourceMap = new HashMap<>();
        final var resource = new Resource();
        final var linksMap = new HashMap<>(
            Map.of("resource", links.self().toString(), VALIDATION_STATUS,
                links.validationStatus().toString()));

        resource.setKind("psc-verification");
        resource.setLinks(linksMap);
        resource.setUpdatedAt(clock.instant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        resourceMap.put(links.self().toString(), resource);

        return resourceMap;
    }

    private static ResponseEntity<PscVerificationApi> createOKResponse(PscVerificationApi filing) {
        final var responseHeaders = new HttpHeaders();

        responseHeaders.setLocation(filing.getLinks().self());

        return ResponseEntity.ok().headers(responseHeaders).body(filing);
    }

}
