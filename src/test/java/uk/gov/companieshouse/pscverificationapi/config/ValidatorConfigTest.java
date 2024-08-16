package uk.gov.companieshouse.pscverificationapi.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.validator.PscExistsValidator;
import uk.gov.companieshouse.pscverificationapi.validator.PscIsActiveValidator;

@ExtendWith(MockitoExtension.class)
class ValidatorConfigTest {

    private ValidatorConfig testConfig;

    @Mock
    private PscExistsValidator pscExistsValidator;
    @Mock
    private PscIsActiveValidator pscIsActiveValidator;

    @BeforeEach
    void setUp() {
        testConfig = new ValidatorConfig();
    }

    @Test
    void verificationValidationEnable() {
        final var valid = testConfig.verificationValidationEnable( pscExistsValidator, pscIsActiveValidator);

        assertThat(valid.pscType(), is(PscType.INDIVIDUAL));
        assertThat(valid.first(), is(pscExistsValidator));
    }
}