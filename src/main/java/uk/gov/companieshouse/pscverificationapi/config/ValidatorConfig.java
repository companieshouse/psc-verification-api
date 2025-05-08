package uk.gov.companieshouse.pscverificationapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.validator.*;

@Configuration
public class ValidatorConfig {

    @Bean
    public ValidationChainEnable verificationValidationEnable(final PscIdProvidedValidator pscIdProvidedValidator,
            final PscExistsValidator pscExistsValidator, final PscIsActiveValidator pscIsActiveValidator,
            final CompanyTypeValidator companyTypeValidator, final CompanyStatusValidator companyStatusValidator,
            final UvidExistsValidator uvidExistsValidator, PscIsUnverifiedValidator pscIsUnverifiedValidator,
            final PscIsPastStartDateValidator pscIsPastStartDateValidator,
            final PscVerificationStatementPresentValidator pscVerificationStatementPresentValidator) {

        createValidationChain(pscIdProvidedValidator, pscExistsValidator, pscIsActiveValidator, companyTypeValidator,
                companyStatusValidator, uvidExistsValidator, pscIsUnverifiedValidator, pscIsPastStartDateValidator,
                pscVerificationStatementPresentValidator);

        return new VerificationValidationChain(PscType.INDIVIDUAL, pscIdProvidedValidator);
    }

    private static void createValidationChain(BaseVerificationValidator... validators) {
        // Link all validators: a->b, b->c, c->d, etc
        for (int i = 0; i < validators.length - 1; i++) {
            validators[i].setNext(validators[i + 1]);
        }
    }

}
