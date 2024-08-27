package uk.gov.companieshouse.pscverificationapi.config.enumerations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;

@Tag("web")
@WebMvcTest
@ContextConfiguration(classes = ConstantsConfig.class)
class ConstantsConfigTest {

    @Autowired
    @Qualifier(value = "companyStatus")
    private Map<String, String> companyStatus;

    @Autowired
    @Qualifier(value = "companyType")
    private Map<String, String> companyType;

    @Test
    void constants() {
        assertThat(companyStatus.get("dissolved"),
            is("Dissolved"));

        assertThat(companyType.get("ltd"),
            is("Private limited company"));
    }

}