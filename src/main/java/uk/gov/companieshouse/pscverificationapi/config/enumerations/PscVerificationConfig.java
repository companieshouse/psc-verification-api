package uk.gov.companieshouse.pscverificationapi.config.enumerations;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.companieshouse.pscverificationapi.enumerations.YamlPropertySourceFactory;

@Configuration
@PropertySource(value = "classpath:api-enumerations/psc_verification.yml", factory = YamlPropertySourceFactory.class)
public class PscVerificationConfig {

    @Bean("validation")
    @ConfigurationProperties(prefix = "validation")
    public Map<String, String> validation() {
        return new HashMap<>();
    }

}
