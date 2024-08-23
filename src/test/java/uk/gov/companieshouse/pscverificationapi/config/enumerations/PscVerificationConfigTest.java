package uk.gov.companieshouse.pscverificationapi.config.enumerations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@ExtendWith(MockitoExtension.class)
class PscVerificationConfigTest {
    @Autowired
    @Qualifier(value = "validation")
    private Map<String, String> validation;

    //FIXME
    @Test
    void pscVerification() {
        assertThat(validation.get("psc-is-ceased"),
            is("PSC is ceased"));
    }

}