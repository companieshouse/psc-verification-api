package uk.gov.companieshouse.pscverificationapi.validator;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.pscverificationapi.service.CompanyProfileService;

import java.util.List;
import java.util.Map;

/**
 * Validator for checking if a company's status is allowed for PSC verification.
 * <p>
 * Uses company profile and configuration to validate status and add errors if not valid.
 * </p>
 */
@Component
public class CompanyStatusValidator extends BaseVerificationValidator implements
    VerificationValidator {

    private final CompanyProfileService companyProfileService;
    private final Map<String, List<String>> company;

    public CompanyStatusValidator(final CompanyProfileService companyProfileService,
                                  @Qualifier(value = "validation") Map<String, String> validation,
                                  @Qualifier(value = "company") Map<String, List<String>> company) {
        super(validation);
        this.companyProfileService = companyProfileService;
        this.validation = validation;
        this.company = company;
    }

    /**
     * Validates if the company status is allowed.
     *
     * @param validationContext the validation context
     */
    @Override
    public void validate(final VerificationValidationContext validationContext) {

        CompanyProfileApi companyProfile = companyProfileService.getCompanyProfile(validationContext.transaction(),
            validationContext.dto(), validationContext.passthroughHeader());

        if (companyProfile != null && company.get("status-not-allowed").contains(companyProfile.getCompanyStatus())) {

            validationContext.errors().add(
                new FieldError("object", "company_status", companyProfile.getCompanyStatus(), false,
                    new String[]{null, "data.company_status"}, null,
                    validation.get("company-status-not-allowed") + companyProfile.getCompanyStatus()));
        }

        super.validate(validationContext);
    }
}
