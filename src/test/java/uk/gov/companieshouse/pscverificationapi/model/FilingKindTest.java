package uk.gov.companieshouse.pscverificationapi.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilingKindTest {

    @Test
    void getValuePscVerificationIndividual() {
        assertThat(FilingKind.PSC_VERIFICATION_INDIVIDUAL.getValue(), is("psc-verification#psc-verification-individual"));
    }

    @Test
    void nameOfPscVerificationIndividualWhenFound() {
        assertThat(FilingKind.nameOf("psc-verification#psc-verification-individual"), is(Optional.of(FilingKind.PSC_VERIFICATION_INDIVIDUAL)));
    }
    @Test
    void getValuePscVerificationRleRo() {
        assertThat(FilingKind.PSC_VERIFICATION_RLE_RO.getValue(), is("psc-verification#psc-verification-rle-ro"));
    }

    @Test
    void nameOfPscVerificationRleRoWhenFound() {
        assertThat(FilingKind.nameOf("psc-verification#psc-verification-rle-ro"), is(Optional.of(FilingKind.PSC_VERIFICATION_RLE_RO)));
    }

    @Test
    void nameOfWhenNotFound() {
        assertThat(FilingKind.nameOf("test"), is(Optional.empty()));
    }

}