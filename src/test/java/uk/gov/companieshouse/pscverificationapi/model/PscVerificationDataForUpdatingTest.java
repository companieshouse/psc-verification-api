package uk.gov.companieshouse.pscverificationapi.model;

import static org.hamcrest.CoreMatchers.equalTo;
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
import uk.gov.companieshouse.pscverificationapi.controller.impl.BaseControllerIT;

@ExtendWith(MockitoExtension.class)
class PscVerificationDataForUpdatingTest extends BaseControllerIT {
    private PscVerificationDataForUpdating testData;
    private VerificationDetailsForUpdating verificationDetails;

    @BeforeEach
    void setUp() {
        verificationDetails = new VerificationDetailsForUpdating("uvid",
            NameMismatchReasonConstants.PREFERRED_NAME,
            EnumSet.of(VerificationStatementConstants.RO_DECLARATION,
                VerificationStatementConstants.RO_IDENTIFIED));
        testData = new PscVerificationDataForUpdating("companyNumber", "pscNotificationId", verificationDetails);
    }


    @Test
    void getCompanyNumber() {
        assertThat(testData.getCompanyNumber(), is("companyNumber"));
    }

    @Test
    void getPscNotificationId() {
        assertThat(testData.getPscNotificationId(), is("pscNotificationId"));
    }

    @Test
    void getVerificationDetails() {
        assertThat(testData.getVerificationDetails(), is(equalTo(verificationDetails)));
    }

    @Test
    void equality() {
        EqualsVerifier.forClass(PscVerificationDataForUpdating.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        assertThat(testData.toString(),
            is("PscVerificationDataForUpdating[companyNumber=companyNumber, "
                + "pscNotificationId=pscNotificationId, "
                + "verificationDetails=VerificationDetailsForUpdating[uvid=uvid, "
                + "nameMismatchReason=PREFERRED_NAME, statements=[RO_IDENTIFIED, "
                + "RO_DECLARATION]]]"));
    }
}