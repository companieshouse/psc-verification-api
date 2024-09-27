package uk.gov.companieshouse.pscverificationapi.validator;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.pscverificationapi.service.CompanyProfileService;

import java.util.List;
import java.util.Map;

@Component
public class CompanyTypeValidator extends BaseVerificationValidator implements
    VerificationValidator {

    private final CompanyProfileService companyProfileService;
    private final Map<String, List<String>> company;

    public CompanyTypeValidator(final CompanyProfileService companyProfileService,
                                @Qualifier(value = "validation") Map<String, String> validation,
                                @Qualifier(value = "company") Map<String, List<String>> company) {
        super(validation);
        this.companyProfileService = companyProfileService;
        this.validation = validation;
        this.company = company;
    }

    /**
     * Validates if the company type is allowed.
     *
     * @param validationContext the validation context
     */
    @Override
    public void validate(final VerificationValidationContext validationContext) {

        CompanyProfileApi companyProfile = companyProfileService.getCompanyProfile(validationContext.transaction(),
            validationContext.dto(), validationContext.passthroughHeader());

        if (companyProfile != null && !company.get("type-allowed").contains(companyProfile.getType())) {

            validationContext.errors().add(
                new FieldError("object", "type", companyProfile.getType(), false,
                    new String[]{null, "data.type"}, null, validation.get("company-type-not-allowed")));
        }

        super.validate(validationContext);
    }
}
