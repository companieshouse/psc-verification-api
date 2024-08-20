package uk.gov.companieshouse.pscverificationapi.interceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.api.AttributeName;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.exception.ConflictingFilingException;
import uk.gov.companieshouse.pscverificationapi.helper.LogMapHelper;
import uk.gov.companieshouse.pscverificationapi.service.CompanyProfileService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Component
public class CompanyInterceptor implements HandlerInterceptor {

    private Map<String, String> validation;
    private Map<String, List<String>> company;
    private CompanyProfileService companyProfileService;
    private final Logger logger;

    public CompanyInterceptor(CompanyProfileService companyProfileService,
                              @Qualifier(value = "validation") Map<String, String> validation,
                              @Qualifier(value = "company") Map<String, List<String>> company,
                              Logger logger) {
        this.companyProfileService = companyProfileService;
        this.validation = validation;
        this.company = company;
        this.logger = logger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        var transaction = (Transaction) request.getAttribute(AttributeName.TRANSACTION.getValue());
        Objects.requireNonNull(transaction, "Transaction missing from request");
        final var passthroughHeader = request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());

        final var logMap = LogMapHelper.createLogMap(transaction.getId());
        logger.info("Company Interceptor", logMap);
        PscVerificationData pscVerificationData = new PscVerificationData("00006400", null, null, null);

        CompanyProfileApi companyProfile = companyProfileService.getCompanyProfile(transaction, pscVerificationData, passthroughHeader);

        if (companyProfile != null && !company.get("type-allowed").contains(companyProfile.getType())) {

            logMap.put("company_number", pscVerificationData.companyNumber());
            logMap.put("company_type", companyProfile.getType());
            logger.info("Company Type not allowed", logMap);
                throw new ConflictingFilingException(createValidationError(
                    validation.get("company-type-not-allowed")));
        }
        return true;
    }

    private List<FieldError> createValidationError(String errorMessage) {
        final var error = new FieldError("ignored", "ignored", errorMessage);
        return List.of(error);
    }

}
