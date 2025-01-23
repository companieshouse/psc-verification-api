package uk.gov.companieshouse.pscverificationapi;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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
        assertThat(app, is(notNullValue()));
    }

}
