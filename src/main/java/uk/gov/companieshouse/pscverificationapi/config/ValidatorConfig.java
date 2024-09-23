package uk.gov.companieshouse.pscverificationapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.validator.PscIdProvidedValidator;
import uk.gov.companieshouse.pscverificationapi.validator.ValidationChainEnable;
import uk.gov.companieshouse.pscverificationapi.validator.VerificationValidationChain;
import uk.gov.companieshouse.pscverificationapi.validator.PscExistsValidator;
import uk.gov.companieshouse.pscverificationapi.validator.PscIsActiveValidator;

@Configuration
public class ValidatorConfig {

    //Currently configured to validate individual PSCs
    //TODO add a 2nd validation chain for the RLE journey if required

    @Bean
    public ValidationChainEnable verificationValidationEnable(final PscIdProvidedValidator pscIdProvidedValidator, final PscExistsValidator pscExistsValidator, final PscIsActiveValidator pscIsActiveValidator) {
        createValidationChain(pscIdProvidedValidator, pscExistsValidator, pscIsActiveValidator);

        return new VerificationValidationChain(PscType.INDIVIDUAL, pscIdProvidedValidator);
    }

    private static void createValidationChain(final PscIdProvidedValidator pscIdProvidedValidator,
        final PscExistsValidator pscExistsValidator, final PscIsActiveValidator pscIsActiveValidator) {
        pscIdProvidedValidator.setNext(pscExistsValidator);

        pscExistsValidator.setNext(pscIsActiveValidator);

    }

}
