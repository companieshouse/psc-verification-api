package uk.gov.companieshouse.pscverificationapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.ClosedTransactionInterceptor;
import uk.gov.companieshouse.api.interceptor.OpenTransactionInterceptor;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.pscverificationapi.interceptor.RequestLoggingInterceptor;

/**
 * Configuration class for interceptor logging.
 */
@Configuration
@ComponentScan("uk.gov.companieshouse.api")
public class InterceptorConfig implements WebMvcConfigurer {
    public static final String COMMON_INTERCEPTOR_PATH =
            "/transactions/{transaction_id}/persons-with-significant-control-verification/{filing_resource_id}";
    public static final String COMMON_INTERCEPTOR_RESOURCE_PATH =
        COMMON_INTERCEPTOR_PATH + "/{filing_resource_id}";
    public static final String PRIVATE_PATH = "/private";
    public static final String FILINGS_PATH = "/filings";
    public static final String FILINGS_RESOURCE_PATH =
        PRIVATE_PATH + COMMON_INTERCEPTOR_PATH + FILINGS_PATH;
    private static final String PSC_VERIFICATION_API = "psc-verification-api";

    /**
     * Set up the interceptors to run against endpoints when the endpoints are called
     * Interceptors are executed in order of configuration
     *
     * @param registry The {@link InterceptorRegistry} to configure
     */
    @Override
    public void addInterceptors(@NonNull final InterceptorRegistry registry) {
        addTransactionInterceptor(registry);
        addOpenTransactionInterceptor(registry);
        addTransactionClosedInterceptor(registry);
        addLoggingInterceptor(registry);
        addInternalUserInterceptor(registry);
    }

    private void addTransactionInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(transactionInterceptor())
            .order(1);
    }

    private void addOpenTransactionInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(openTransactionInterceptor())
            .addPathPatterns(COMMON_INTERCEPTOR_PATH, COMMON_INTERCEPTOR_RESOURCE_PATH).order(2);
    }

    private void addTransactionClosedInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(transactionClosedInterceptor())
            .addPathPatterns(FILINGS_RESOURCE_PATH).order(3);
    }

    private void addLoggingInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor())
            .order(4);
    }

    void addInternalUserInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(new InternalUserInterceptor())
            .addPathPatterns(FILINGS_RESOURCE_PATH).order(6);
    }

    @Bean("chsTransactionInterceptor")
    public TransactionInterceptor transactionInterceptor() {
        return new TransactionInterceptor(PSC_VERIFICATION_API);
    }

    @Bean
    public OpenTransactionInterceptor openTransactionInterceptor() {
        return new OpenTransactionInterceptor(PSC_VERIFICATION_API);
    }

    @Bean
    public ClosedTransactionInterceptor transactionClosedInterceptor() {
        return new ClosedTransactionInterceptor(FILINGS_PATH);
    }

    @Bean("chsLoggingInterceptor")
    public RequestLoggingInterceptor requestLoggingInterceptor() {
        return new RequestLoggingInterceptor();
    }
}