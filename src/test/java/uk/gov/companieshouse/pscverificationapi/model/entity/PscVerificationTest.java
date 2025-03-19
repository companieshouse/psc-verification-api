package uk.gov.companieshouse.pscverificationapi.model.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.common.ResourceLinks;
import uk.gov.companieshouse.api.model.psc.NameElementsApi;
import uk.gov.companieshouse.api.model.pscverification.InternalData;
import uk.gov.companieshouse.api.model.pscverification.NameMismatchReasonConstants;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.RelevantOfficer;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants;

@ExtendWith(MockitoExtension.class)
class PscVerificationTest {
    private static final Instant INSTANT = Instant.parse("2024-01-01T10:08:42Z");
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 12, 31);
    private static final EnumSet<VerificationStatementConstants> STATEMENTS_INDIVIDUAL = EnumSet.of(
        VerificationStatementConstants.INDIVIDUAL_VERIFIED);
    private static final EnumSet<VerificationStatementConstants> STATEMENTS_RO = EnumSet.of(
        VerificationStatementConstants.RO_DECLARATION);

    private PscVerification testVerification;
    private ResourceLinks links;
    private PscVerificationData data;
    private InternalData internalData;
    private VerificationDetails verif;
    private RelevantOfficer relevantOfficer;
    private NameElementsApi nameElements;

    @BeforeEach
    void setUp() {
        links = ResourceLinks.newBuilder()
            .self(URI.create("self"))
            .validationStatus(URI.create("valid"))
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
        relevantOfficer = RelevantOfficer.newBuilder()
            .nameElements(nameElements)
            .dateOfBirth(DATE_OF_BIRTH)
            .isEmployee(true)
            .isDirector(true)
            .build();
        data = PscVerificationData.newBuilder()
            .companyNumber("company-number")
            .pscNotificationId("psc-notification-id")
            .verificationDetails(verif)
            .relevantOfficer(relevantOfficer)
            .build();
        internalData = InternalData.newBuilder().internalId("123").build();
        testVerification = PscVerification.newBuilder()
            .id("id")
            .createdAt(INSTANT)
            .updatedAt(INSTANT)
            .links(links)
            .data(data)
            .internalData(internalData)
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
                + "links=ResourceLinks[self=self, validationStatus=valid], "
                + "data=PscVerificationData[companyNumber=company-number, "
                + "pscNotificationId=psc-notification-id, "
                + "relevantOfficer=RelevantOfficer["
                + "nameElements=NameElementsApi[title='Sir', forename='Forename', "
                + "otherForenames='Other Forenames', middleName='Middlename', "
                + "surname='Surname'], "
                + "dateOfBirth=1990-12-31, isEmployee=true, isDirector=true], "
                + "verificationDetails=VerificationDetails[uvid='uvid', "
                + "nameMismatchReason=PREFERRED_NAME, statements=[INDIVIDUAL_VERIFIED]]], "
                + "internalData=InternalData[internalId=123]]"));
    }
}