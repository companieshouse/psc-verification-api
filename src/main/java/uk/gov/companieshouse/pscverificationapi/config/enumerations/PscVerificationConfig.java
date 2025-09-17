package uk.gov.companieshouse.pscverificationapi.config.enumerations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.companieshouse.pscverificationapi.enumerations.YamlPropertySourceFactory;

/**
 * Configuration class for loading PSC verification properties from YAML files.
 * <p>
 * This class is responsible for loading and providing access to PSC verification
 * properties defined in the `api-enumerations/psc_verification.yml` via Spring's
 * {@link ConfigurationProperties}.
 * </p>
 */
@Configuration
@PropertySource(value = "classpath:api-enumerations/psc_verification.yml", factory = YamlPropertySourceFactory.class)
public class PscVerificationConfig {

    @Bean("validation")
    @ConfigurationProperties(prefix = "validation")
    public Map<String, String> validation() {
        return new HashMap<>();
    }

    @Bean("company")
    @ConfigurationProperties(prefix = "company")
    public  Map<String, List<String>> company() {
        return new HashMap<>();
    }

}
