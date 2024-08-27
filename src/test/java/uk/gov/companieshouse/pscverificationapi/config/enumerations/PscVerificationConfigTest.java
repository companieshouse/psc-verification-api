package uk.gov.companieshouse.pscverificationapi.config.enumerations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;

//Using Spring Web MVC
@Tag("web")
@WebMvcTest
@ContextConfiguration(classes = PscVerificationConfig.class)
class PscVerificationConfigTest {
    @Autowired
    @Qualifier(value = "validation")
    private Map<String, String> validation;

    @Test
    void pscVerification() {
        assertThat(validation.get("etag-not-match"),
            is("ETag for PSC must match the latest value"));
    }

}