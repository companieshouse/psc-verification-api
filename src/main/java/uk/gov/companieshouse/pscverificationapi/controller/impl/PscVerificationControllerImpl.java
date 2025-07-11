package uk.gov.companieshouse.pscverificationapi.controller.impl;

import static org.springframework.util.ObjectUtils.isEmpty;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.api.model.common.ResourceLinks;
import uk.gov.companieshouse.api.model.psc.PscIndividualFullRecordApi;
import uk.gov.companieshouse.api.model.pscverification.InternalData;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.patch.model.PatchResult;
import uk.gov.companieshouse.pscverificationapi.controller.PscVerificationController;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.error.RetrievalFailureReason;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.exception.InvalidFilingException;
import uk.gov.companieshouse.pscverificationapi.exception.InvalidPatchException;
import uk.gov.companieshouse.pscverificationapi.exception.PscLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.helper.LogMapHelper;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.model.mapper.PscVerificationMapper;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;
import uk.gov.companieshouse.pscverificationapi.service.TransactionService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@RestController
@RequestMapping("/transactions/{transactionId}/persons-with-significant-control-verification")
public class PscVerificationControllerImpl implements PscVerificationController {
    public static final String VALIDATION_STATUS = "validation_status";
    private static final String PATCH_RESULT_MSG = "PATCH result";
    private static final String STATUS_MSG = "status";
    private static final String PATCH_FAILED = "patch failed";
    private static final String ERROR_MSG = "error";

    private final TransactionService transactionService;
    private final PscVerificationService pscVerificationService;
    private final PscLookupService pscLookupService;
    private final PscVerificationMapper filingMapper;
    private final Clock clock;
    private final Logger logger;

    public PscVerificationControllerImpl(final TransactionService transactionService,
        final PscVerificationService pscVerificationService, final PscLookupService pscLookupService,
        PscVerificationMapper filingMapper, final Clock clock, final Logger logger) {
            this.transactionService = transactionService;
            this.pscVerificationService = pscVerificationService;
            this.pscLookupService = pscLookupService;
            this.filingMapper = filingMapper;
            this.clock = clock;
            this.logger = logger;
    }

    @Override
    @Transactional
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PscVerificationApi> createPscVerification(
        @PathVariable("transactionId") final String transId,
        @RequestAttribute(required = false, name = "transaction") final Transaction transaction,
        @RequestBody @Valid @NotNull final PscVerificationData data, final BindingResult result,
        final HttpServletRequest request) {

        final var logMap = LogMapHelper.createLogMap(transId);
        logMap.put("path", request.getRequestURI());
        logMap.put("method", request.getMethod());
        logger.debugRequest(request, "POST", logMap);

        Optional.ofNullable(result).ifPresent(PscVerificationControllerImpl::checkBindingErrors);

        final var requestTransaction = getTransaction(transId, transaction, logMap,
            getPassthroughHeader(request));

        final var entity = filingMapper.toEntity(data);
        final PscIndividualFullRecordApi pscIndividualFullRecordApi;
        try {
            pscIndividualFullRecordApi = pscLookupService.getPscIndividualFullRecord(
                    requestTransaction, data, PscType.INDIVIDUAL);
        } catch (PscLookupServiceException e) {
            logMap.put("psc_notification_id", data.pscNotificationId());
            logger.errorContext(String.format("PSC Id %s does not have an Internal ID in PSC Data API for company number %s",
                    data.pscNotificationId(), data.companyNumber()), null, logMap);
            throw new PscLookupServiceException(
                    "We are currently unable to process a Verification filing for this PSC", new Exception("Internal Id"));
        }

        var internalData = InternalData.newBuilder().internalId(String.valueOf(pscIndividualFullRecordApi.getInternalId())).build();
        entity.setInternalData(internalData);

        final var savedEntity = saveFilingWithLinks(entity, transId, request, logMap);

        if (transaction != null) {
            updateTransactionResources(requestTransaction, savedEntity.getLinks());
        }

        final var response = filingMapper.toApi(savedEntity);

        return ResponseEntity.created(savedEntity.getLinks().self()).body(response);
    }

    @Override
    @Transactional
    @PatchMapping(value = "/{filingResourceId}", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = "application/merge-patch+json")
    public ResponseEntity<PscVerificationApi> updatePscVerification(
            @PathVariable("transactionId") final String transId,
            @PathVariable("filingResourceId") final String filingResource,
            @RequestBody final @NotNull Map<String, Object> mergePatch,
            final HttpServletRequest request) {

        final var logMap = LogMapHelper.createLogMap(transId);
        Optional<PscVerification> pscVerification = pscVerificationService.get(filingResource);

        PscIndividualFullRecordApi pscIndividualFullRecordApi = null;
        if (mergePatch.get("psc_notification_id") != null && pscVerification.isPresent()) {
            final var transaction = getTransaction(transId, null, logMap, getPassthroughHeader(request));

            String companyNumber = mergePatch.get("company_number") != null
                    ? mergePatch.get("company_number").toString()
                    : pscVerification.orElseThrow().getData().companyNumber();
            final var dataToLookup = PscVerificationData.newBuilder(pscVerification.orElseThrow().getData())
                    .pscNotificationId(mergePatch.get("psc_notification_id").toString())
                    .companyNumber(companyNumber)
                    .build();

            pscIndividualFullRecordApi = pscLookupService.getPscIndividualFullRecord(
                    transaction, dataToLookup, PscType.INDIVIDUAL);

            if (pscIndividualFullRecordApi.getInternalId() == null) {
                logMap.put("psc_notification_id", mergePatch.get("psc_notification_id"));
                logger.errorContext(String.format("PSC Id %s does not have an Internal ID in PSC Data API for company number %s",
                        mergePatch.get("psc_notification_id"), companyNumber),null, logMap);
                throw new PscLookupServiceException(
                        "We are currently unable to process a Verification filing for this PSC", new Exception("Internal Id"));
            }
        }

        pscVerification.ifPresent(v -> clearNameMismatchReasonIfRequired(v, mergePatch));

        final var patchResult = pscVerification.filter(
                f1 -> pscVerificationService.requestMatchesResourceSelf(request, f1)).map(
                f -> pscVerificationService.patch(filingResource, mergePatch)).orElse(
                new PatchResult(RetrievalFailureReason.FILING_NOT_FOUND));

        if (patchResult.failedRetrieval()) {
            final var reason = (RetrievalFailureReason) patchResult.getRetrievalFailureReason();

            logMap.put(STATUS_MSG, PATCH_FAILED);
            logMap.put(ERROR_MSG, "retrieval failure: " + reason);
            logger.debugContext(transId, PATCH_RESULT_MSG, logMap);

            throw new FilingResourceNotFoundException(filingResource);
        }
        else if (patchResult.failedValidation()) {

            @SuppressWarnings("unchecked")
            final var errors = (List<FieldError>) patchResult.getValidationErrors();

            logMap.put(STATUS_MSG, PATCH_FAILED);
            logMap.put(ERROR_MSG, "validation failure: " + errors);
            logger.debugContext(transId, PATCH_RESULT_MSG, logMap);

            throw new InvalidPatchException(errors);
        }
        else {
            logMap.put(STATUS_MSG, "patch successful");
            logger.debugContext(transId, PATCH_RESULT_MSG, logMap);

            final var optionalFiling = pscVerificationService.get(filingResource);

            if (mergePatch.get("psc_notification_id") != null && pscIndividualFullRecordApi != null) {
                var internalData = InternalData.newBuilder()
                        .internalId(String.valueOf(pscIndividualFullRecordApi.getInternalId()))
                        .build();
                optionalFiling.ifPresent(v -> v.setInternalData(internalData));
                pscVerificationService.save(optionalFiling.orElseThrow());
            }

            return optionalFiling
                    .map(filingMapper::toApi)
                    .map(PscVerificationControllerImpl::createOKResponse)
                    .orElse(ResponseEntity.notFound()
                            .build());
        }
    }

    private static void clearNameMismatchReasonIfRequired(PscVerification pscVerification, Map<String, Object> mergePatch) {

        VerificationDetails verificationDetails = pscVerification.getData().verificationDetails();

        if (verificationDetails != null) {
            @SuppressWarnings("unchecked")
            final var mergeVerificationDetails = (Map<String, Object>) mergePatch.get("verification_details");
            final var mergeUvid = mergeVerificationDetails != null ? (String) mergeVerificationDetails.get("uvid") : null;

            if (verificationDetails.uvid() != null && mergeUvid != null
                && !verificationDetails.uvid().equals(mergeUvid)
                && isEmpty(mergeVerificationDetails.get("name_mismatch_reason"))) {

                mergeVerificationDetails.put("name_mismatch_reason", null);
            }
        }
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

        return ResourceLinks.newBuilder().self(selfUri).validationStatus(validateUri)
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

    private static ResponseEntity<PscVerificationApi> createOKResponse(
        final PscVerificationApi filing) {
        final var responseHeaders = new HttpHeaders();

        responseHeaders.setLocation(filing.getLinks().self());

        return ResponseEntity.ok().headers(responseHeaders).body(filing);
    }

}
