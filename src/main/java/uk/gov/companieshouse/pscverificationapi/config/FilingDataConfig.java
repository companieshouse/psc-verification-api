package uk.gov.companieshouse.pscverificationapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for filing data.
 */
@Configuration
public class FilingDataConfig {
    @Value("${filing.data.description}")
    private String pscVerificationDescription;

    public String getPscVerificationDescription() {
        return pscVerificationDescription;
    }

    public void setPscVerificationDescription(String pscVerificationDescription) {
        this.pscVerificationDescription = pscVerificationDescription;
    }
}
