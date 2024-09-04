package uk.gov.companieshouse.pscverificationapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.MappablePermissionsInterceptor;
import uk.gov.companieshouse.api.interceptor.OpenTransactionInterceptor;
import uk.gov.companieshouse.api.interceptor.PermissionsMapping;
import uk.gov.companieshouse.api.interceptor.TokenPermissionsInterceptor;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.pscverificationapi.interceptor.ClosedTransactionInterceptor;
import uk.gov.companieshouse.pscverificationapi.interceptor.RequestLoggingInterceptor;

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
    public static final String FILINGS_RESOURCE_PATH =
        "/private" + COMMON_INTERCEPTOR_RESOURCE_PATH + "/filings";
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
        addTokenPermissionsInterceptor(registry);
        addRequestPermissionsInterceptor(registry);
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

    private void addTokenPermissionsInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(tokenPermissionsInterceptor())
            .order(3);
    }
    private void addRequestPermissionsInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(requestPermissionsInterceptor(pscPermissionsMapping()))
            .order(4);
    }

    private void addTransactionClosedInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(transactionClosedInterceptor())
            .addPathPatterns(FILINGS_RESOURCE_PATH).order(5);
    }

    private void addLoggingInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor())
            .order(6);
    }

    void addInternalUserInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(new InternalUserInterceptor())
            .addPathPatterns(FILINGS_RESOURCE_PATH).order(7);
    }

    @Bean("chsTransactionInterceptor")
    public TransactionInterceptor transactionInterceptor() {
        return new TransactionInterceptor(PSC_VERIFICATION_API);
    }

    @Bean ("chsOpenTransactionInterceptor")
    public OpenTransactionInterceptor openTransactionInterceptor() {
        return new OpenTransactionInterceptor(PSC_VERIFICATION_API);
    }

    @Bean("chsTokenPermissionInterceptor")
    public TokenPermissionsInterceptor tokenPermissionsInterceptor() {
        return new TokenPermissionsInterceptor();
    }

    @Bean("chsRequestPermissionInterceptor")
    public MappablePermissionsInterceptor requestPermissionsInterceptor(
        final PermissionsMapping permissionMapping) {
        return new MappablePermissionsInterceptor(Permission.Key.USER_PSC_VERIFICATION, true,
            permissionMapping);
    }

    @Bean("chsPermissionsMapping")
    public PermissionsMapping pscPermissionsMapping() {
        return PermissionsMapping.builder()
            .defaultRequireAnyOf(Permission.Value.CREATE)
            .build();
    }

    @Bean ("chsClosedTransactionInterceptor")
    public ClosedTransactionInterceptor transactionClosedInterceptor() {
        return new ClosedTransactionInterceptor(FILINGS_RESOURCE_PATH);
    }

    @Bean("chsLoggingInterceptor")
    public RequestLoggingInterceptor requestLoggingInterceptor() {
        return new RequestLoggingInterceptor();
    }
}