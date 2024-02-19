package uk.gov.companieshouse.pscverificationapi.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import java.time.LocalDate;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.psc.NameElementsApi;
import uk.gov.companieshouse.pscverificationapi.controller.impl.BaseControllerIT;

@ExtendWith(MockitoExtension.class)
class RelevantOfficerForUpdatingTest extends BaseControllerIT {
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 1, 4);
    private RelevantOfficerForUpdating testOfficer;
    private NameElementsApi nameElements;

    @BeforeEach
    void setUp() {
        nameElements = createNameElements("Sir", "Forename", "Other Forenames", "Surname");
        testOfficer = new RelevantOfficerForUpdating(nameElements, DATE_OF_BIRTH, true, true);

    }

    @Test
    void getNameElements() {
        assertThat(testOfficer.getNameElements(), samePropertyValuesAs(nameElements));
    }

    @Test
    void getDateOfBirth() {
        assertThat(testOfficer.getDateOfBirth(), is(DATE_OF_BIRTH));
    }

    @Test
    void getIsEmployee() {
        assertThat(testOfficer.getIsEmployee(), is(true));
    }

    @Test
    void getIsDirector() {
        assertThat(testOfficer.getIsDirector(), is(true));
    }

    @Test
    void equality() {
        EqualsVerifier.forClass(RelevantOfficerForUpdating.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        assertThat(testOfficer.toString(),
            is("RelevantOfficerForUpdating[nameElements=NameElementsApi[title='Sir', "
                + "forename='Forename', otherForenames='Other Forenames', middleName='null', "
                + "surname='Surname'], dateOfBirth=1990-01-04, isEmployee=true, isDirector=true]"));
    }
}