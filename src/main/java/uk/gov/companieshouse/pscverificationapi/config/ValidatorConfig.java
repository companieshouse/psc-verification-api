package uk.gov.companieshouse.pscverificationapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.validator.CompanyStatusValidator;
import uk.gov.companieshouse.pscverificationapi.validator.CompanyTypeValidator;
import uk.gov.companieshouse.pscverificationapi.validator.PscIdProvidedValidator;
import uk.gov.companieshouse.pscverificationapi.validator.UvidExistsValidator;
import uk.gov.companieshouse.pscverificationapi.validator.ValidationChainEnable;
import uk.gov.companieshouse.pscverificationapi.validator.VerificationValidationChain;
import uk.gov.companieshouse.pscverificationapi.validator.PscExistsValidator;
import uk.gov.companieshouse.pscverificationapi.validator.PscIsActiveValidator;
import uk.gov.companieshouse.pscverificationapi.validator.PscIsUnverifiedValidator;

@Configuration
public class ValidatorConfig {

    // Currently configured to validate individual PSCs
    // TODO add a 2nd validation chain for the RLE journey if required

    @Bean
    public ValidationChainEnable verificationValidationEnable(final PscIdProvidedValidator pscIdProvidedValidator,
            final PscExistsValidator pscExistsValidator, final PscIsActiveValidator pscIsActiveValidator,
            final CompanyTypeValidator companyTypeValidator, final CompanyStatusValidator companyStatusValidator,
            final UvidExistsValidator uvidExistsValidator, PscIsUnverifiedValidator pscIsUnverifiedValidator) {

        createValidationChain(pscIdProvidedValidator, pscExistsValidator, pscIsActiveValidator, companyTypeValidator,
                companyStatusValidator, uvidExistsValidator, pscIsUnverifiedValidator);

        return new VerificationValidationChain(PscType.INDIVIDUAL, pscIdProvidedValidator);
    }

    private static void createValidationChain(final PscIdProvidedValidator pscIdProvidedValidator,
            final PscExistsValidator pscExistsValidator, final PscIsActiveValidator pscIsActiveValidator,
            final CompanyTypeValidator companyTypeValidator, final CompanyStatusValidator companyStatusValidator,
            final UvidExistsValidator uvidExistsValidator, final PscIsUnverifiedValidator pscIsUnverifiedValidator) {

        pscIdProvidedValidator.setNext(pscExistsValidator);
        pscExistsValidator.setNext(pscIsActiveValidator);
        pscIsActiveValidator.setNext(companyTypeValidator);
        companyTypeValidator.setNext(companyStatusValidator);
        companyStatusValidator.setNext(uvidExistsValidator);
        uvidExistsValidator.setNext(pscIsUnverifiedValidator);

    }

}
