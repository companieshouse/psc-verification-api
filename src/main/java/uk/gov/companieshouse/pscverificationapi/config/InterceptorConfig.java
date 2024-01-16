package uk.gov.companieshouse.pscverificationapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;

/**
 * Configuration class for interceptor logging.
 */
@Configuration
@ComponentScan("uk.gov.companieshouse.api")
public class InterceptorConfig implements WebMvcConfigurer {
    public static final String COMMON_INTERCEPTOR_PATH =
            "/transactions/{transaction_id}/persons-with-significant-control-verification";
    public static final String COMMON_INTERCEPTOR_RESOURCE_PATH =
            COMMON_INTERCEPTOR_PATH + "/{filing_resource_id}";
    /**
     * Set up the interceptors to run against endpoints when the endpoints are called
     * Interceptors are executed in order of configuration
     *
     * @param registry The {@link InterceptorRegistry} to configure
     */
    @Override
    public void addInterceptors(@NonNull final InterceptorRegistry registry) {
//        addTransactionInterceptor(registry);
    }

    private void addTransactionInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(transactionInterceptor())
                .order(1);
    }


    @Bean("chsTransactionInterceptor")
    public TransactionInterceptor transactionInterceptor() {
        return new TransactionInterceptor("psc-verification-api");
    }
}