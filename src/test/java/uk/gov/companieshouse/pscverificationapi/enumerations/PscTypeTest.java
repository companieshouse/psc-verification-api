package uk.gov.companieshouse.pscverificationapi.enumerations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.companieshouse.pscverificationapi.enumerations.PscType.INDIVIDUAL;

import org.junit.jupiter.api.Test;

class PscTypeTest {

    @Test
    void nameOf() {
        final var pscName = PscType.nameOf("individual");
        assertThat(pscName.get(), is(INDIVIDUAL));
    }

}