package uk.gov.companieshouse.pscverificationapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
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

    /**
     * Customize the serialization of {@link java.time.Instant} values to have accuracy to
     * milliseconds.
     *
     * @return the custom {@link Jackson2ObjectMapperBuilderCustomizer} bean.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer addCustomTimeSerialization() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.serializerByType(
            Instant.class, new JsonSerializer<Instant>() {

                private final DateTimeFormatter formatter =
                    new DateTimeFormatterBuilder().appendInstant(
                    3).toFormatter();

                @Override
                public void serialize(final Instant instant, final JsonGenerator generator,
                    final SerializerProvider provider) throws IOException {
                    generator.writeString(formatter.format(instant));
                }
            });
    }

    @Bean("postObjectMapper")
    @Primary
    public ObjectMapper objectMapper() {
        return objectMapperBuilder().build();
    }

    @Bean("patchObjectMapper")
    public ObjectMapper patchObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule())
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS)
                ;
    }
}
