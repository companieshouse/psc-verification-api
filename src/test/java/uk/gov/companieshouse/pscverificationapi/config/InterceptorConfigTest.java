package uk.gov.companieshouse.pscverificationapi.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.OpenTransactionInterceptor;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.pscverificationapi.interceptor.ClosedTransactionInterceptor;
import uk.gov.companieshouse.pscverificationapi.interceptor.RequestLoggingInterceptor;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
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
    void addInterceptorsInvocations() {

        verify(interceptorRegistry, times(1)).addInterceptor(any(TransactionInterceptor.class));
        verify(interceptorRegistry, times(1)).addInterceptor(any(OpenTransactionInterceptor.class));
        verify(interceptorRegistry, times(1)).addInterceptor(any(ClosedTransactionInterceptor.class));
        verify(interceptorRegistry, times(1)).addInterceptor(any(RequestLoggingInterceptor.class));
        verify(interceptorRegistry, times(1)).addInterceptor(any(InternalUserInterceptor.class));
    }

    @Test
    void addInterceptors() {

        verify(interceptorRegistry.addInterceptor(any(OpenTransactionInterceptor.class))
            .addPathPatterns(
                "/transactions/{transaction_id}/persons-with-significant-control-verification",
                "/transactions/{transaction_id}/persons-with-significant-control-verification"
                    + "/{filing_resource_id}")).order(2);
        verify(interceptorRegistry.addInterceptor(any(ClosedTransactionInterceptor.class))
            .addPathPatterns("/private"
                + "/transactions/{transaction_id}/persons-with-significant-control-verification"
                + "/{filing_resource_id}/filings")).order(5);
        verify(interceptorRegistry.addInterceptor(any(RequestLoggingInterceptor.class))).order(6);
        verify(interceptorRegistry.addInterceptor(any(InternalUserInterceptor.class))
            .addPathPatterns("/private/transactions/{transaction_id}/persons-with-significant-control-verification/{filing_resource_id}/filings")).order(7);
    }

    @Test
    void testTransactionInterceptor() {
        assertThat(testConfig.transactionInterceptor(), isA(TransactionInterceptor.class));
    }

    @Test
    void openTransactionInterceptor() {
        assertThat(testConfig.openTransactionInterceptor(), isA(OpenTransactionInterceptor.class));
    }

    @Test
    void closedTransactionInterceptor() {
        assertThat(testConfig.transactionClosedInterceptor(), isA(ClosedTransactionInterceptor.class));
    }

    @Test
    void requestLoggingInterceptor() {
        assertThat(testConfig.requestLoggingInterceptor(), isA(RequestLoggingInterceptor.class));
    }

}