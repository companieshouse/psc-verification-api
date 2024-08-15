package uk.gov.companieshouse.pscverificationapi.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.pscverificationapi.helper.LogMapHelper;
import uk.gov.companieshouse.pscverificationapi.service.CompanyProfileService;

import java.io.IOException;

@Service
public class CompanyProfileServiceImpl implements CompanyProfileService {

    private final ApiClientService apiClientService;
    private final Logger logger;

    public CompanyProfileServiceImpl(ApiClientService apiClientService, Logger logger) {
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    @Override
    public CompanyProfileApi getCompanyProfile(final Transaction transaction, final PscVerificationData pscVerificationData, final String ericPassThroughHeader)
            throws CompanyProfileServiceException {
        final var logMap = LogMapHelper.createLogMap(transaction.getId());

        String companyNumber = pscVerificationData.companyNumber();

        try {
            final String uri = "/company/" + companyNumber;
            final CompanyProfileApi companyProfile = apiClientService.getApiClient(ericPassThroughHeader)
                            .company()
                            .get(uri)
                            .execute()
                            .getData();
            logMap.put("company_number", companyNumber);
            logMap.put("company_name", transaction.getCompanyName());
            logger.debugContext(transaction.getId(), "Retrieved company profile details", logMap);
            return companyProfile;
        }
        catch (final URIValidationException | IOException e) {
            throw new CompanyProfileServiceException("Error Retrieving company profile " + companyNumber, e);
        }
    }
}
