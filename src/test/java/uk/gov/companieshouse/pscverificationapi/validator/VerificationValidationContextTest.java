package uk.gov.companieshouse.pscverificationapi.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;

@ExtendWith(MockitoExtension.class)
class VerificationValidationContextTest {
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private VerificationValidationContext testContext;
    private List<FieldError> errors;
    @Mock
    private PscVerificationData dto;
    @Mock
    private Transaction transaction;


    @BeforeEach
    void setUp() {
        errors = new ArrayList<>();
        testContext = new VerificationValidationContext(dto, errors, transaction, PscType.INDIVIDUAL, PASSTHROUGH_HEADER);
    }

    @Test
    void getDto() {
        assertThat(testContext.dto(), is(sameInstance(dto)));
    }

    @Test
    void getErrors() {
        assertThat(testContext.errors(), is(errors));
    }

    @Test
    void getTransaction() {
        assertThat(testContext.transaction(), is(sameInstance(transaction)));
    }

    @Test
    void getPscType() {
        assertThat(testContext.pscType(), is(PscType.INDIVIDUAL));
    }

    @Test
    void getPassthroughHeader() {
        assertThat(testContext.passthroughHeader(), is(PASSTHROUGH_HEADER));
    }

    @Test
    void testEqualsVerificationValidationContext() {
        EqualsVerifier.forClass(VerificationValidationContext.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        assertThat(testContext.toString(),
            is("VerificationValidationContext[dto=dto, errors=[], transaction=transaction, pscType=INDIVIDUAL, passthroughHeader='passthrough']"));
    }

    @Test
    void testHashCode() {
        EqualsVerifier.forClass(VerificationValidationContext.class).verify();
    }
    
}
