package uk.gov.companieshouse.pscverificationapi.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.validator.CompanyValidator;
import uk.gov.companieshouse.pscverificationapi.validator.PscExistsValidator;
import uk.gov.companieshouse.pscverificationapi.validator.PscIdProvidedValidator;
import uk.gov.companieshouse.pscverificationapi.validator.PscIsActiveValidator;

@ExtendWith(MockitoExtension.class)
class ValidatorConfigTest {

    private ValidatorConfig testConfig;

    @Mock
    PscIdProvidedValidator pscIdProvidedValidator;
    @Mock
    private PscExistsValidator pscExistsValidator;
    @Mock
    private PscIsActiveValidator pscIsActiveValidator;
    @Mock
    private CompanyValidator companyValidator;

    @BeforeEach
    void setUp() {
        testConfig = new ValidatorConfig();
    }

    @Test
    void verificationValidationEnable() {
        final var valid = testConfig.verificationValidationEnable(pscIdProvidedValidator, pscExistsValidator,
            pscIsActiveValidator, companyValidator);

        assertThat(valid.pscType(), is(PscType.INDIVIDUAL));
        assertThat(valid.first(), is(pscIdProvidedValidator));
        verify(pscExistsValidator, times(1)).setNext(pscIsActiveValidator);
        verify(pscIsActiveValidator, times(1)).setNext(companyValidator);
    }

}