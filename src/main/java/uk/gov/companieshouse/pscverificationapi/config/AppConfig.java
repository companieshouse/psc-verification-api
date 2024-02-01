package uk.gov.companieshouse.pscverificationapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.api.sdk.impl.ApiClientServiceImpl;

@Configuration
@EnableTransactionManagement
public class AppConfig {
    /**
     * Obtains a clock that returns the current instant using the best available
     * system clock, converting to date and time using the UTC time-zone.
     *
     * @return a clock that uses the best available system clock in the UTC zone, not null
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    MongoTransactionManager transactionManager(final MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    @Bean
    public ApiClientService apiClientService() {
        return new ApiClientServiceImpl();
    }

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder().serializationInclusion(
                        JsonInclude.Include.NON_NULL)
                .simpleDateFormat("yyyy-MM-dd")
                .failOnUnknownProperties(true) // override Spring Boot default (false)
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Bean("postObjectMapper")
    @Primary
    public ObjectMapper objectMapper() {
        return objectMapperBuilder().build();
    }

    @Bean
    @Qualifier("patchObjectMapper")
    public ObjectMapper patchObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule())
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS)
//                .setDefaultMergeable(Boolean.TRUE)
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
//                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                ;
    }
}
