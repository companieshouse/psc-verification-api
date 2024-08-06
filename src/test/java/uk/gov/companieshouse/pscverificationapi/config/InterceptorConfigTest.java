package uk.gov.companieshouse.pscverificationapi.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.pscverificationapi.interceptor.RequestLoggingInterceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InterceptorConfigTest {

    private InterceptorConfig testConfig;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private InterceptorRegistry interceptorRegistry;


    @BeforeEach
    void setUp() {
        testConfig = new InterceptorConfig();
        testConfig.addInterceptors(interceptorRegistry);
    }

    @Test
    public void addInterceptors() {

        verify(interceptorRegistry, times(1)).addInterceptor(any(TransactionInterceptor.class));
        verify(interceptorRegistry, times(1)).addInterceptor(any(RequestLoggingInterceptor.class));
        verify(interceptorRegistry, times(1)).addInterceptor(any(InternalUserInterceptor.class));
    }

    @Test
    void addInterceptorsOrder() {

        verify(interceptorRegistry.addInterceptor(any(TransactionInterceptor.class))).order(1);
        verify(interceptorRegistry.addInterceptor(any(RequestLoggingInterceptor.class))).order(2);
        verify(interceptorRegistry.addInterceptor(any(InternalUserInterceptor.class))
            .addPathPatterns("/private/transactions/{transaction_id}/persons-with-significant-control-verification/{filing_resource_id}/filings")).order(6);

    }
}