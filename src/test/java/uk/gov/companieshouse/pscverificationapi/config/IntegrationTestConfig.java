package uk.gov.companieshouse.pscverificationapi.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.gov.companieshouse.pscverificationapi.mapper.ErrorMapper;
import uk.gov.companieshouse.pscverificationapi.mapper.ErrorMapperImpl;
import uk.gov.companieshouse.pscverificationapi.model.mapper.PscVerificationMapper;
import uk.gov.companieshouse.pscverificationapi.model.mapper.PscVerificationMapperImpl;

@TestConfiguration
public class IntegrationTestConfig {

    @Bean
    public PscVerificationMapper pscVerificationMapper() {
        return new PscVerificationMapperImpl();
    }

    @Bean
    public ErrorMapper errorMapper() {
        return new ErrorMapperImpl();
    }
}
