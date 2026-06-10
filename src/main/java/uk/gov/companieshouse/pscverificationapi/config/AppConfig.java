package uk.gov.companieshouse.pscverificationapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.json.JsonMapper;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;

/**
 * Configuration class for application-wide beans and settings.
 */
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

    private static SimpleModule instantModule() {
        final var formatter = new DateTimeFormatterBuilder().appendInstant(3).toFormatter();
        return new SimpleModule().addSerializer(Instant.class, new ValueSerializer<Instant>() {
            @Override
            public void serialize(final Instant instant, final JsonGenerator generator,
                final SerializationContext provider) {
                generator.writeString(formatter.format(instant));
            }
        });
    }

    @Bean("postObjectMapper")
    @Primary
    public JsonMapper objectMapper() {
        return JsonMapper.builder()
            .findAndAddModules()
            .addModule(instantModule())
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true) // override Spring Boot default (false)
            .build();
    }

    @Bean("patchObjectMapper")
    public ObjectMapper patchObjectMapper() {
        return JsonMapper.builder()
            .findAndAddModules()
            .addModule(instantModule())
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.ALWAYS).withValueInclusion(JsonInclude.Include.ALWAYS))
            .build()
                ;
    }

    @Bean("environmentReader")
    public EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }
}
