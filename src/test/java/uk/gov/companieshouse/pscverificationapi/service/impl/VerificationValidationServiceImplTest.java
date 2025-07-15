package uk.gov.companieshouse.pscverificationapi.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.service.VerificationValidationService;
import uk.gov.companieshouse.pscverificationapi.validator.ValidationChainEnable;
import uk.gov.companieshouse.pscverificationapi.validator.VerificationValidationChain;
import uk.gov.companieshouse.pscverificationapi.validator.VerificationValidationContext;
import uk.gov.companieshouse.pscverificationapi.validator.VerificationValidator;

@ExtendWith(MockitoExtension.class)
class VerificationValidationServiceImplTest {

    private VerificationValidationService testService;
    @Mock
    private VerificationValidator firstValidator;
    @Mock
    private VerificationValidationContext context;

    @BeforeEach
    void setUp() {
        List<? extends ValidationChainEnable> verificationValidators =
            List.of(new VerificationValidationChain(PscType.INDIVIDUAL, firstValidator));
        testService = new VerificationValidationServiceImpl(verificationValidators);

    }

    @Test
    void validateWhenPscTypeSupported() {
        when(context.pscType()).thenReturn(PscType.INDIVIDUAL);

        testService.validate(context);

        verify(firstValidator).validate(context);
    }

    @Test
    void validateWhenPscTypeNotSupported() {
        when(context.pscType()).thenReturn(null);

        final var exception = assertThrows(UnsupportedOperationException.class,
            () -> testService.validate(context));

        assertThat(exception.getMessage(), is("Validation not defined for PSC type 'null'"));

    }

}