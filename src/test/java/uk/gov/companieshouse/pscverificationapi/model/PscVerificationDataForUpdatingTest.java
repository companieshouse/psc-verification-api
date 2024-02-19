package uk.gov.companieshouse.pscverificationapi.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
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
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 1, 4);
    private PscVerificationDataForUpdating testData;
    private RelevantOfficerForUpdating relevantOfficer;
    private VerificationDetailsForUpdating verificationDetails;

    @BeforeEach
    void setUp() {
        relevantOfficer = new RelevantOfficerForUpdating(
            createNameElements("Sir", "Forename", "Other Forenames", "Surname"), DATE_OF_BIRTH,
            true, true);
        verificationDetails = new VerificationDetailsForUpdating("uvid",
            NameMismatchReasonConstants.PREFERRED_NAME,
            EnumSet.of(VerificationStatementConstants.RO_DECLARATION,
                VerificationStatementConstants.RO_IDENTIFIED));
        testData = new PscVerificationDataForUpdating("companyNumber", "pscAppointmentId",
            relevantOfficer, verificationDetails);
    }


    @Test
    void getCompanyNumber() {
        assertThat(testData.getCompanyNumber(), is("companyNumber"));
    }

    @Test
    void getPscAppointmentId() {
        assertThat(testData.getPscAppointmentId(), is("pscAppointmentId"));
    }

    @Test
    void getRelevantOfficer() {
        assertThat(testData.getRelevantOfficer(), is(equalTo(relevantOfficer)));
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
                + "pscAppointmentId=pscAppointmentId, "
                + "relevantOfficer=RelevantOfficerForUpdating[nameElements=NameElementsApi[title"
                + "='Sir', forename='Forename', otherForenames='Other Forenames', "
                + "middleName='null', surname='Surname'], dateOfBirth=1990-01-04, "
                + "isEmployee=true, isDirector=true], "
                + "verificationDetails=VerificationDetailsForUpdating[uvid=uvid, "
                + "nameMismatchReason=PREFERRED_NAME, statements=[RO_IDENTIFIED, "
                + "RO_DECLARATION]]]"));
    }
}