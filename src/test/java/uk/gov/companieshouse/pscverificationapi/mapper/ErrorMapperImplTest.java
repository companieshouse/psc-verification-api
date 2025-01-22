package uk.gov.companieshouse.pscverificationapi.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;

@ExtendWith(MockitoExtension.class)
class ErrorMapperTest {
    private ErrorMapper testMapper;

    private FieldError fieldError;

    @BeforeEach
    void setUp() {
        testMapper = Mappers.getMapper(ErrorMapper.class);
        fieldError = new FieldError("message", "field", "Default message");
    }

    @Test
    void mapFieldError() {
        final var validationStatusError = testMapper.map(fieldError);
        final var expectedError =
            new ValidationStatusError("Default message", "$.field", "json-path", "ch:validation");

        assertThat(validationStatusError, samePropertyValuesAs(expectedError));
    }

    @Test
    void mapFieldErrorWhenNull() {
        assertThat(testMapper.map((FieldError) null), is(nullValue()));
    }

    @Test
    void mapFieldErrorList() {
        final var validationStatusErrors = testMapper.map(Set.of(fieldError));
        final var expectedError =
            new ValidationStatusError("Default message", "$.field", "json-path", "ch:validation");

        assertThat(validationStatusErrors,
            is(arrayContaining(samePropertyValuesAs(expectedError))));
    }

    @Test
    void mapFieldErrorListWhenNull() {
        assertThat(testMapper.map((Set<FieldError>) null), is(nullValue()));
    }
}