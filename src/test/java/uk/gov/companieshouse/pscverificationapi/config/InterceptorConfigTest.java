package uk.gov.companieshouse.pscverificationapi.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import uk.gov.companieshouse.pscverificationapi.interceptor.RequestLoggingInterceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InterceptorConfigTest {

    private InterceptorConfig testConfig;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private InterceptorRegistry interceptorRegistry;

    @BeforeEach
    void setUp() {
        testConfig = new InterceptorConfig();
    }

    @Test
    void addInterceptors() {
        testConfig.addInterceptors(interceptorRegistry);

        verify(interceptorRegistry.addInterceptor(any(RequestLoggingInterceptor.class))).order(2);
    }
}