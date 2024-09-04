package uk.gov.companieshouse.pscverificationapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import uk.gov.companieshouse.pscverificationapi.enumerations.YamlPropertySourceFactory;
import uk.gov.companieshouse.pscverificationapi.mapper.ErrorMapper;
import uk.gov.companieshouse.pscverificationapi.mapper.ErrorMapperImpl;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
@PropertySource(value = "classpath:api-enumerations/psc_verification.yml", factory = YamlPropertySourceFactory.class)
public class IntegrationTestConfig {

    @Bean("validation")
    @ConfigurationProperties(prefix = "validation")
    public Map<String, String> validation() {
        return new HashMap<>();
    }

    @Bean
    public ErrorMapper errorMapper() {
        return new ErrorMapperImpl();
    }
}
