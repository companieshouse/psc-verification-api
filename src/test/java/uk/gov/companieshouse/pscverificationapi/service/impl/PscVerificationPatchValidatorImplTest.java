package uk.gov.companieshouse.pscverificationapi.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.SmartValidator;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.model.mapper.PscVerificationMapper;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationPatchValidator;

@ExtendWith(MockitoExtension.class)
class PscVerificationPatchValidatorImplTest {
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 5, 5);

    private PscVerificationPatchValidator testValidator;

    @Mock
    private SmartValidator validator;
    @Mock
    private PscVerificationMapper mapper;

    @BeforeEach
    void setUp() {
        testValidator = new PscVerificationPatchValidatorImpl(validator, mapper);
    }

    @Test
    void validateWhenValid() {
        final var dummyFiling = PscVerification.newBuilder().build();
        final var dummyApi = PscVerificationApi.newBuilder().build();

        when(mapper.toApi(dummyFiling)).thenReturn(dummyApi);

        final var result = testValidator.validate(dummyFiling);

        assertThat(result.isSuccess(), is(true));
    }

    @Test
    void validateWhenNotValid() {
        final var dummyFiling = PscVerification.newBuilder().build();
        final var dummyApi = PscVerificationApi.newBuilder().build();
        final var expectedError = new FieldError("patched", "data", null, false,
            new String[]{"future.date.patched.data", "future.date.data",
                "future.date."
                    + PscVerificationData.class.getName(), "future.date"},
            new Object[]{TEST_DATE}, "bad data");

        when(mapper.toApi(dummyFiling)).thenReturn(dummyApi);
        doAnswer(i -> {
            final Errors errors = i.getArgument(1);
            errors.rejectValue("data", "future.date", new Object[]{TEST_DATE}, "bad data");
            return null;
        }).when(validator).validate(any(), any());

        final var result = testValidator.validate(dummyFiling);
        @SuppressWarnings(
            "unchecked") final var error = ((List<FieldError>) result.getErrors()).getFirst();

        assertThat(result.isSuccess(), is(false));
        assertThat(error.getCodes(), is(equalTo(expectedError.getCodes())));
        assertThat(error, is(equalTo(expectedError)));
    }

}