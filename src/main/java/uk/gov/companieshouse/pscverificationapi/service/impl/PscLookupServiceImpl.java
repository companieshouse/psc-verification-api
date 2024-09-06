package uk.gov.companieshouse.pscverificationapi.service.impl;

import java.io.IOException;
import java.text.MessageFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.psc.PscApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceInvalidException;
import uk.gov.companieshouse.pscverificationapi.exception.PscLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;
import uk.gov.companieshouse.pscverificationapi.utils.LogHelper;

//TODO This service is currently stubbed out to return the following responses:
// i) A valid PSC for all IDs except for:
// ii) PSC with ID '1kdaTltWeaP1EB70SSD9SLmiK5Z' - this is mocked as ceased
// ii) PSC with ID 'doesNotExist' - this is mocked as a PSC that does not exist

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

        } catch (URIValidationException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
