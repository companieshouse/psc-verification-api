package uk.gov.companieshouse.pscverificationapi.error;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.error.ApiError;

@ExtendWith(MockitoExtension.class)
class ApiErrorsTest {

    private ApiErrors testErrors;

    @BeforeEach
    void setUp() {
        testErrors = new ApiErrors();
    }

    @Test
    void noArgConstructor() {
        assertThat(testErrors.getErrors(), is(Collections.emptySet()));
    }

    @Test
    @DisplayName("add(): unique elements only")
    void add() {
        final var error = new ApiError();

        testErrors.add(error);
        testErrors.add(error);

        assertThat(testErrors.getErrors(), contains(error));
    }

    @Test
    @DisplayName("add(): null arg not allowed")
    void addWhenNull() {
        assertThrows(NullPointerException.class, () -> testErrors.add(null));
    }

    @Test
    void addAll() {
        final var error1 = new ApiError("error1", null, null, "test");
        final var error2 = new ApiError("error2", null, null, "test");
        final var errors = List.of(error1, error2);

        testErrors.addAll(errors);

        assertThat(testErrors.getErrors(), contains(error1, error2));
    }

    @Test
    void hasErrors() {
        final var error = new ApiError();

        assertThat(testErrors.hasErrors(), is(false));

        testErrors.add(error);

        assertThat(testErrors.hasErrors(), is(true));
    }

    @Test
    void containsError() {
        final var error = new ApiError();

        assertThat(testErrors.contains(error), is(false));

        testErrors.add(error);

        assertThat(testErrors.contains(error), is(true));
    }

    @Test
    @DisplayName("contains(): null arg not allowed")
    void containsWhenNull() {
        assertThrows(NullPointerException.class, () -> testErrors.contains(null));
    }

    @Test
    @DisplayName("getErrors(): unique elements")
    void getErrors() {
        final var error = new ApiError();

        assertThat(testErrors.getErrors(), is(Collections.emptySet()));

        testErrors.add(error);
        testErrors.add(error);

        assertThat(testErrors.getErrors(), contains(error));
    }

    @Test
    @DisplayName("getErrorCount(): unique elements")
    void getErrorCount() {
        final var error = new ApiError();

        assertThat(testErrors.getErrorCount(), is(0));

        testErrors.add(error);
        testErrors.add(error);

        assertThat(testErrors.getErrorCount(), is(1));
    }

}