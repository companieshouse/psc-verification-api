package uk.gov.companieshouse.pscverificationapi.service.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.identityverification.model.UvidMatch;
import uk.gov.companieshouse.api.model.psc.PscApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.exception.PscLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;
import uk.gov.companieshouse.pscverificationapi.utils.LogHelper;

@Service
public class PscLookupServiceImpl implements PscLookupService {
    private static final String UNEXPECTED_STATUS_CODE = "Unexpected Status Code received";

    private final ApiClientService apiClientService;
    private final Logger logger;

    public PscLookupServiceImpl(final ApiClientService apiClientService, Logger logger) {
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    /**
     * Retrieve a PSC by PscVerificationData.
     *
     * @param transaction       the Transaction
     * @param data              the PSC verification data
     * @param pscType           the PSC Type
     * @param ericPassThroughHeader includes authorisation for transaction fetch
     * @return the PSC details, if found
     * @throws PscLookupServiceException if the PSC was not found or an error occurred
     */
    @Override
    public PscApi getPsc(final Transaction transaction, final PscVerificationData data,
                                         final PscType pscType, final String ericPassThroughHeader)
        throws PscLookupServiceException {

        final var logMap = LogHelper.createLogMap(transaction.getId());
        String pscAppointmentId = data.pscAppointmentId();

        try {
            final var uri = "/company/"
                + data.companyNumber()
                + "/persons-with-significant-control/"
                + pscType.getValue()
                + "/"
                + pscAppointmentId;

            return apiClientService.getApiClient(ericPassThroughHeader)
                .pscs()
                .getIndividual(uri)
                .execute()
                .getData();

        } catch (final ApiErrorResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
                logger.errorContext(transaction.getId(), UNEXPECTED_STATUS_CODE, e, logMap);
                throw new FilingResourceNotFoundException(
                    MessageFormat.format("PSC Details not found for {0}: {1} {2}", pscAppointmentId,
                        e.getStatusCode(), e.getStatusMessage()), e);
            }
            throw new PscLookupServiceException(
                MessageFormat.format("Error Retrieving PSC details for {0}: {1} {2}", pscAppointmentId,
                    e.getStatusCode(), e.getStatusMessage()), e);

        } catch (URIValidationException | IOException e) {
            logger.errorContext(transaction.getId(), UNEXPECTED_STATUS_CODE, e, logMap);
            throw new PscLookupServiceException(
                MessageFormat.format("Error Retrieving PSC details for {0}: {1}", pscAppointmentId,
                    e.getMessage()), e);
        }
    }

    /**
     * Retrieve a UvidMatch by PscVerificationData.
     *
     * @param transaction       the Transaction
     * @param data              the PSC verification data
     * @param pscType           the PSC Type
     * @param ericPassThroughHeader includes authorisation for transaction fetch
     * @return the UvidMatch, if found
     * @throws PscLookupServiceException if the PSC was not found or an error occurred
     */
    @Override
    public UvidMatch getUvidMatchFromPscData(final Transaction transaction,
        final PscVerificationData data, final PscType pscType, final String ericPassThroughHeader)
        throws PscLookupServiceException {

        PscApi pscData = getPsc(transaction, data, pscType, ericPassThroughHeader);
        UvidMatch uvidMatch;
        uvidMatch = getUvidMatch(pscData);

        return uvidMatch;
    }

    //TODO Update UvidMatch with the full DOB when available from the PscDetails
    private UvidMatch getUvidMatch(PscApi pscData) {
        UvidMatch uvidMatch = new UvidMatch();

        List<String> forenames = new ArrayList<>();
        Optional<String> forename = Optional.ofNullable(pscData.getNameElements().getForename());
        Optional<String> otherForenames = Optional.ofNullable(pscData.getNameElements().getOtherForenames());
        Optional<String> surname = Optional.ofNullable(pscData.getNameElements().getSurname());

        forename.ifPresent(forenames::add);
        otherForenames.ifPresent(names -> forenames.addAll(Arrays.asList(names.split("\\s+"))));

        uvidMatch.setForenames(forenames);
        uvidMatch.setSurname(surname.orElse(""));

        return uvidMatch;
    }
}
