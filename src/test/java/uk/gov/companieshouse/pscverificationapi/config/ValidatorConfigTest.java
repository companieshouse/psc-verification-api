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
import uk.gov.companieshouse.pscverificationapi.validator.CompanyStatusValidator;
import uk.gov.companieshouse.pscverificationapi.validator.CompanyTypeValidator;
import uk.gov.companieshouse.pscverificationapi.validator.PscExistsValidator;
import uk.gov.companieshouse.pscverificationapi.validator.PscIdProvidedValidator;
import uk.gov.companieshouse.pscverificationapi.validator.PscIsActiveValidator;
import uk.gov.companieshouse.pscverificationapi.validator.UvidExistsValidator;

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
    private CompanyTypeValidator companyTypeValidator;
    @Mock
    private CompanyStatusValidator companyStatusValidator;
    @Mock
    private UvidExistsValidator uvidExistsValidator;

    @BeforeEach
    void setUp() {
        testConfig = new ValidatorConfig();
    }

    @Test
    void verificationValidationEnable() {
        final var valid = testConfig.verificationValidationEnable(pscIdProvidedValidator, pscExistsValidator,
            pscIsActiveValidator, companyTypeValidator, companyStatusValidator, uvidExistsValidator);

        assertThat(valid.pscType(), is(PscType.INDIVIDUAL));
        assertThat(valid.first(), is(pscIdProvidedValidator));
        verify(pscExistsValidator, times(1)).setNext(pscIsActiveValidator);
        verify(pscIsActiveValidator, times(1)).setNext(companyTypeValidator);
        verify(companyTypeValidator, times(1)).setNext(companyStatusValidator);
        verify(companyStatusValidator, times(1)).setNext(uvidExistsValidator);
    }

}