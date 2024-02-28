package uk.gov.companieshouse.pscverificationapi.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilingDataConfigTest {

    private FilingDataConfig filingDataConfig;

    @BeforeEach
    void setUp() {
        filingDataConfig = new FilingDataConfig();
    }

    @Test
    void getPscVerificationDescription() {
        filingDataConfig.setPscVerificationDescription("PSC Verification");

        assertThat(filingDataConfig.getPscVerificationDescription(), is("PSC Verification"));
    }

}

