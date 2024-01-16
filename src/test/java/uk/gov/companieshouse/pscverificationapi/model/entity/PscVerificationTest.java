package uk.gov.companieshouse.pscverificationapi.model.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

import java.time.Instant;
import java.util.Set;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.psc.NameElementsApi;
import uk.gov.companieshouse.api.model.pscverification.NameMismatchReasonConstants;
import uk.gov.companieshouse.api.model.pscverification.PersonalDetails;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationLinks;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants;

@ExtendWith(MockitoExtension.class)
class PscVerificationTest {
    private static final Instant INSTANT = Instant.parse("2024-01-01T10:08:42Z");
    private static final Set<VerificationStatementConstants> STATEMENTS_INDIVIDUAL = Set.of(
        VerificationStatementConstants.INDIVIDUAL_VERIFIED);
    private static final Set<VerificationStatementConstants> STATEMENTS_RO = Set.of(
        VerificationStatementConstants.RO_DECLARATION);
    private PscVerification testVerification;
    private PscVerificationLinks links;
    private PscVerificationData data;
    private VerificationDetails verif;
    private PersonalDetails personal;
    private NameElementsApi nameElements;

    @BeforeEach
    void setUp() {
        links = PscVerificationLinks.newBuilder()
            .self("self")
            .validationStatus("valid")
            .build();
        verif = VerificationDetails.newBuilder()
            .uvid("uvid")
            .nameMismatchReason(NameMismatchReasonConstants.PREFERRED_NAME)
            .statements(STATEMENTS_INDIVIDUAL)
            .build();
        nameElements = new NameElementsApi();
        nameElements.setTitle("Sir");
        nameElements.setForename("Forename");
        nameElements.setOtherForenames("Other Forenames");
        nameElements.setMiddleName("Middlename");
        nameElements.setSurname("Surname");
        personal = PersonalDetails.newBuilder()
            .nameElements(nameElements)
            .build();
        data = PscVerificationData.newBuilder()
            .companyNumber("company-number")
            .pscAppointmentId("psc-appointment-id")
            .verificationDetails(verif)
            .personalDetails(personal)
            .build();
        testVerification = PscVerification.newBuilder()
            .id("id")
            .createdAt(INSTANT)
            .updatedAt(INSTANT)
            .links(links)
            .data(data)
            .build();
    }

    @Test
    void newBuilder() {
        assertThat(PscVerification.newBuilder(), isA(PscVerification.Builder.class));
    }

    @Test
    void newBuilderCopy() {
        final var copy = PscVerification.newBuilder(testVerification).build();

        assertThat(copy, is(equalTo(testVerification)));
    }

    @Test
    void equality() {
        EqualsVerifier.simple().forClass(PscVerification.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        assertThat(testVerification.toString(),
            is("PscVerification[id='id', createdAt=2024-01-01T10:08:42Z, "
                + "updatedAt=2024-01-01T10:08:42Z, "
                + "links=PscVerificationLinks[validationStatus='valid', self='self'], "
                + "data=PscVerificationData[companyNumber=company-number, "
                + "pscAppointmentId=psc-appointment-id, "
                + "personalDetails=PersonalDetails[appointmentId='appointment-id', "
                + "nameElements=NameElementsApi[title='Sir', forename='Forename', "
                + "otherForenames='Other Forenames', middleName='Middlename', "
                + "surname='Surname']], verificationDetails=VerificationDetails[uvid=uvid, "
                + "nameMismatchReason=PREFERRED_NAME, statements=[INDIVIDUAL_VERIFIED]]]]"));
    }
}