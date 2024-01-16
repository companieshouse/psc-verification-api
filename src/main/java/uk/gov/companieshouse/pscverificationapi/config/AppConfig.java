package uk.gov.companieshouse.pscverificationapi.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
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

}
