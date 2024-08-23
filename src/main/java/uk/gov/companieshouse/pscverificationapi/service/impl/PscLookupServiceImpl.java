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

//TODO This service is currently mocked out to return the following mock values:
// i) A valid PSC with ID 1kdaTltWeaP1EB70SSD9SLmiK5Y
// ii) A PSC that has ceased with ID 1kdaTltWeaP1EB70SSD9SLmiK5Z
// ii) An invalid PSC for all other PSCs

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

        if (pscId.matches("1kdaTltWeaP1EB70SSD9SLmiK5Y")) {
            return new PscApi();

        } else if (pscId.matches("1kdaTltWeaP1EB70SSD9SLmiK5Z")) {
            PscApi ceasedPsc = new PscApi();
            ceasedPsc.setCeasedOn(LocalDate.of(2024,1,21));
            return ceasedPsc;

        } else {
            throw new FilingResourceNotFoundException(
                MessageFormat.format("PSC Details not found for {0}: {1} {2}", pscId,
                    HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase()));

        }

    }
}
