package uk.gov.companieshouse.pscverificationapi.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.EnumSet;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.pscverification.NameMismatchReasonConstants;
import uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants;

@ExtendWith(MockitoExtension.class)
class VerificationDetailsForUpdatingTest {
    private VerificationDetailsForUpdating testDetails;

    @BeforeEach
    void setUp() {
        testDetails = new VerificationDetailsForUpdating("uvid",
            NameMismatchReasonConstants.PREFERRED_NAME,
            EnumSet.of(VerificationStatementConstants.RO_DECLARATION,
                VerificationStatementConstants.RO_IDENTIFIED));
    }

    @Test
    void getUvid() {
        assertThat(testDetails.getUvid(), is("uvid"));
    }

    @Test
    void getNameMismatchReason() {
        assertThat(testDetails.getNameMismatchReason(),
            is(NameMismatchReasonConstants.PREFERRED_NAME));
    }

    @Test
    void getVerificationStatements() {
        assertThat(testDetails.getVerificationStatements(),
            is(EnumSet.of(VerificationStatementConstants.RO_DECLARATION,
                VerificationStatementConstants.RO_IDENTIFIED)));
    }

    @Test
    void equality() {
        EqualsVerifier.forClass(VerificationDetailsForUpdating.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        assertThat(testDetails.toString(),
            is("VerificationDetailsForUpdating[uvid=uvid, nameMismatchReason=PREFERRED_NAME, "
                + "statements=[RO_IDENTIFIED, RO_DECLARATION]]"));
    }
}