package uk.gov.companieshouse.pscverificationapi.service.impl;

import java.io.IOException;
import java.text.MessageFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.psc.PscApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.exception.PscLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;

@Service
public class PscLookupServiceImpl implements PscLookupService {
    private static final String UNEXPECTED_STATUS_CODE = "Unexpected Status Code received";

    private final ApiClientService apiClientService;
    private final Logger logger;

    public PscLookupServiceImpl(final ApiClientService apiClientService, Logger logger) {
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    @Override
    public PscApi getPsc(final Transaction transaction, final PscVerificationData data,
                                         final PscType pscType, final String ericPassThroughHeader)
        throws PscLookupServiceException {

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
                throw new FilingResourceNotFoundException(
                    MessageFormat.format("PSC Details not found for {0}: {1} {2}", pscAppointmentId,
                        e.getStatusCode(), e.getStatusMessage()), e);
            }
            throw new PscLookupServiceException(
                MessageFormat.format("Error Retrieving PSC details for {0}: {1} {2}", pscAppointmentId,
                    e.getStatusCode(), e.getStatusMessage()), e);

        } catch (URIValidationException | IOException e) {
            throw new PscLookupServiceException(
                MessageFormat.format("Error Retrieving PSC details for {0}: {1}", pscAppointmentId,
                    e.getMessage()), e);
        }
    }
}
