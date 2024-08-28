package uk.gov.companieshouse.pscverificationapi.service.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.psc.PscApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.exception.PscLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;
import uk.gov.companieshouse.pscverificationapi.utils.LogHelper;

//TODO This service is currently stubbed out to return the following responses:
// i) A valid PSC with all IDs except for:
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

    //FIXME: Remove temporary stubbing of this service
    @Override
    public PscApi getPsc(final Transaction transaction, final String pscId,
        final PscType pscType, final String ericPassThroughHeader)
        throws PscLookupServiceException {

        final var logMap = LogHelper.createLogMap(transaction.getId());
        PscApi psc;

        if (pscId.matches("1kdaTltWeaP1EB70SSD9SLmiK5Z")) {
            psc = new PscApi();
            psc.setCeasedOn(LocalDate.of(2024, 1, 21));
            return psc;
        } else if (pscId.matches("doesNotExist")) {
            throw new FilingResourceNotFoundException(
                MessageFormat.format("PSC Details not found for {0}: {1} {2}", pscId,
                    HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase()));
        } else {
            psc = new PscApi();
            return psc;
        }
    }
}
