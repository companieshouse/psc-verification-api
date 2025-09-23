package uk.gov.companieshouse.pscverificationapi.service.impl;

import java.text.MessageFormat;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.psc.IndividualFullRecord;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.exception.PscLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.sdk.companieshouse.InternalApiClientService;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;
import uk.gov.companieshouse.pscverificationapi.utils.LogHelper;

/**
 * Interacts with the PSC Data API to retrieve PSCs.
 * <p>
 * Implements {@link PscLookupService}
 * </p>
 */
@Service
public class PscLookupServiceImpl implements PscLookupService {
    private static final String UNEXPECTED_STATUS_CODE = "Unexpected Status Code received";

    private final InternalApiClientService apiClientService;
    private final Logger logger;

    public PscLookupServiceImpl(InternalApiClientService apiClientService, Logger logger) {
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    @Override
    public IndividualFullRecord getIndividualFullRecord(final Transaction transaction, final PscVerificationData data,
                                                             final PscType pscType)
            throws PscLookupServiceException {

        final var logMap = LogHelper.createLogMap(transaction.getId());
        String pscNotificationId = data.pscNotificationId();

        try {
            final var uri = "/company/"
                    + data.companyNumber()
                    + "/persons-with-significant-control/"
                    + pscType.getValue()
                    + "/"
                    + pscNotificationId
                    + "/full_record";

            return apiClientService.getInternalApiClient()
                .privatePscFullRecordResourceHandler()
                .getPscFullRecord(uri)
                .execute()
                .getData();


        } catch (final ApiErrorResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
                logger.errorContext(transaction.getId(), UNEXPECTED_STATUS_CODE, e, logMap);
                throw new FilingResourceNotFoundException(
                        MessageFormat.format("PSC Details not found for {0}: {1} {2}", pscNotificationId,
                                e.getStatusCode(), e.getStatusMessage()), e);
            }
            throw new PscLookupServiceException(
                    MessageFormat.format("Error Retrieving PSC details for {0}: {1} {2}", pscNotificationId,
                            e.getStatusCode(), e.getStatusMessage()), e);

        } catch (URIValidationException e) {
            logger.errorContext(transaction.getId(), UNEXPECTED_STATUS_CODE, e, logMap);
            throw new PscLookupServiceException(
                    MessageFormat.format("Error Retrieving PSC details for {0}: {1}", pscNotificationId,
                            e.getMessage()), e);
        }
    }
}
