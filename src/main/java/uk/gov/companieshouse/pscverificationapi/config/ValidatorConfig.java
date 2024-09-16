package uk.gov.companieshouse.pscverificationapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.validator.ValidationChainEnable;
import uk.gov.companieshouse.pscverificationapi.validator.VerificationValidationChain;
import uk.gov.companieshouse.pscverificationapi.validator.PscExistsValidator;
import uk.gov.companieshouse.pscverificationapi.validator.PscIsActiveValidator;

@Configuration
public class ValidatorConfig {

    //TODO - handle the psc type better?

    @Bean
    public ValidationChainEnable verificationValidationEnable(final PscExistsValidator pscExistsValidator, final PscIsActiveValidator pscIsActiveValidator) {
        createValidationChain(pscExistsValidator, pscIsActiveValidator);

        return new VerificationValidationChain(PscType.INDIVIDUAL, pscExistsValidator);
    }

    private static void createValidationChain(final PscExistsValidator pscExistsValidator, final PscIsActiveValidator pscIsActiveValidator) {

        //Suspend further validation when a PSC does not exist
        if(pscExistsValidator.isValid()) {
            pscExistsValidator.setNext(pscIsActiveValidator);
        }

    }

}
