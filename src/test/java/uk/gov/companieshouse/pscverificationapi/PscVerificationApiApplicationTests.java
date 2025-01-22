package uk.gov.companieshouse.pscverificationapi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@Tag("app")
@SpringBootTest
class PscVerificationApiApplicationTests {

    @Mock
    private PscVerificationApiApplication app;

    @Test
    void contextLoads() {
        // This test will fail if the application context cannot start
        assertNotNull(app);
    }

}
