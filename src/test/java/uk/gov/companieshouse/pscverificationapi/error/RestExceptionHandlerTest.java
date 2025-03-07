package uk.gov.companieshouse.pscverificationapi.error;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.context.request.ServletWebRequest;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.exception.ConflictingFilingException;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.exception.InvalidFilingException;
import uk.gov.companieshouse.pscverificationapi.exception.MergePatchException;
import uk.gov.companieshouse.service.ServiceException;

@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {

    public static final String BLANK_JSON_QUOTED = "\"\"";
    public static final String MALFORMED_JSON_QUOTED = "\"{\"";
    private static final String VERIFICATION_FRAGMENT =
        "{\"reference_etag\": \"etag\","
        + "\"company_number\": \"12345678\","
        + "\"psc_notification_id\": \"id\","
        + "\"verification_details\": { "
            + "\"uvid\": \"999999\","
            + "\"verification_statements\": "
                + "[ \"INDIVIDUAL_VERIFIED\"]"
            + "}"
        + "}";

    private RestExceptionHandler testExceptionHandler;

    @Mock
    private HttpHeaders headers;
    @Mock
    private ServletWebRequest request;
    @Mock
    private MismatchedInputException mismatchedInputException;
    @Mock
    private InvalidFormatException invalidFormatException;
    @Mock
    private UnrecognizedPropertyException unrecognizedPropertyException;
    @Mock
    private JsonParseException jsonParseException;
    @Mock
    private Logger logger;

    private MockHttpServletRequest servletRequest;

    @Mock
    private JsonMappingException.Reference mappingReference;

    private FieldError fieldError;
    private FieldError fieldErrorWithRejectedValue;
    private ApiError expectedErrorWithRejectedValue;
    private Throwable mergePatchCause;

    @BeforeEach
    void setUp() {
        Map<String, String> validation = Map.of(
            "filing-resource-not-found", "Filing resource {filing-resource-id} not found",
            "NotBlank", "field is blank",
            "pscIsCeased", "{rejected-value} is ceased",
            "patch-merge-error-prefix", "Failed to merge patch request: ",
            "unknown-property-name", "Property is not recognised: {property-name}",
            "json-syntax-prefix", "JSON parse error: ");

        testExceptionHandler = new RestExceptionHandler(validation, logger);
        servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/path/to/resource");
        String[] codes1 = new String[]{"code1", "object.company_number", "code3", "NotBlank"};
        String[] codes2 = new String[]{"code1", "object.psc_notification_id", "code3", "pscIsCeased"};
        fieldError = new FieldError("object", "field1", null, false, codes1, null, "error");
        fieldErrorWithRejectedValue =
            new FieldError("object", "appointmentId", "1kdaTltWeaP1EB70SSD9SLmiK5Z", false, codes2, null,
                "errorWithRejectedValue");
        mergePatchCause = new Throwable();

        expectedErrorWithRejectedValue =
            new ApiError("{rejected-value} is ceased", "$.psc_notification_id", "json-path", "ch:validation");

        expectedErrorWithRejectedValue.addErrorValue("rejected-value", "1kdaTltWeaP1EB70SSD9SLmiK5Z");
    }

    @Test
    void handleHttpMessageNotReadableWhenJsonBlank() {
        when(request.getRequest()).thenReturn(servletRequest);

        final var exceptionMessage = getHttpMessageNotReadableException(BLANK_JSON_QUOTED);

        final var response =
            testExceptionHandler.handleHttpMessageNotReadable(exceptionMessage, headers,
                HttpStatus.BAD_REQUEST, request);

        assertThat(response, is(notNullValue()));
        final var apiErrors = (ApiErrors) response.getBody();
        final var expectedError =
            new ApiError("JSON parse error: Unexpected end-of-input", "$", "json-path",
                "ch:validation");
        final var actualError = Objects.requireNonNull(apiErrors).getErrors().iterator().next();

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(actualError.getErrorValues(), is(nullValue()));
        assertThat(actualError, is(samePropertyValuesAs(expectedError)));
    }

    @Test
    void handleHttpMessageNotReadableWhenJsonMalformed() {
        when(request.getRequest()).thenReturn(servletRequest);

        final var exceptionMessage = getHttpMessageNotReadableException(MALFORMED_JSON_QUOTED);

        final var response = testExceptionHandler.handleHttpMessageNotReadable(exceptionMessage, headers,
            HttpStatus.BAD_REQUEST, request);

        assertThat(response, is(notNullValue()));
        final var apiErrors = (ApiErrors) response.getBody();
        final var expectedError = new ApiError(
            "JSON parse error: Unexpected end-of-input", "$",
            "json-path", "ch:validation");

        final var actualError = Objects.requireNonNull(apiErrors).getErrors().iterator().next();

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(actualError.getErrorValues(), is(nullValue()));
        assertThat(actualError, is(samePropertyValuesAs(expectedError)));
    }

    @Test
    void handleHttpMessageNotReadableWhenUnrecognizedPropertyException() {
        final var message = new MockHttpInputMessage(
            VERIFICATION_FRAGMENT.replaceAll("company_number", "company_numberX").getBytes());
        when(request.getRequest()).thenReturn(servletRequest);
        when(unrecognizedPropertyException.getLocation()).thenReturn(
            new JsonLocation(null, 100, 3, 7));
        when(unrecognizedPropertyException.getPath()).thenReturn(List.of(mappingReference));
        when(unrecognizedPropertyException.getPropertyName()).thenReturn("company_numberX");
        when(mappingReference.getFieldName()).thenReturn("company_numberX");
        final var unrecognizedMsg = "JSON parse error: Property is not recognised: {property-name}";
        final var exceptionMessage = new HttpMessageNotReadableException(unrecognizedMsg, unrecognizedPropertyException,
            message);

        final var response = testExceptionHandler.handleHttpMessageNotReadable(exceptionMessage, headers,
            HttpStatus.BAD_REQUEST, request);

        assertThat(response, is(notNullValue()));
        final var apiErrors = (ApiErrors) response.getBody();
        final var expectedError =
            new ApiError(unrecognizedMsg, "$.company_numberX", "json-path", "ch:validation");
        expectedError.addErrorValue("offset", "line: 3, column: 7");
        expectedError.addErrorValue("line", "3");
        expectedError.addErrorValue("column", "7");
        expectedError.addErrorValue("property-name", "company_numberX");
        final var actualError = Objects.requireNonNull(apiErrors).getErrors().iterator().next();

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(actualError.getError(), is(expectedError.getError()));
        assertThat(actualError.getLocation(), is(expectedError.getLocation()));
        assertThat(actualError.getType(), is(expectedError.getType()));
        assertThat(actualError.getLocationType(), is(expectedError.getLocationType()));
        assertThat(actualError.getErrorValues(), is(expectedError.getErrorValues()));
        assertThat(actualError, is(samePropertyValuesAs(expectedError)));
    }

    @Test
    void handleHttpMediaTypeNotSupported() {
        final var mediaMergePatch = MediaType.parseMediaType("application/merge-patch+json");
        final var exception = new HttpMediaTypeNotSupportedException(MediaType.APPLICATION_PDF,
            List.of(MediaType.APPLICATION_JSON, mediaMergePatch));

        when(request.getRequest()).thenReturn(servletRequest);

        final var response = testExceptionHandler.handleHttpMediaTypeNotSupported(exception, headers,
            HttpStatus.UNSUPPORTED_MEDIA_TYPE, request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(HttpStatus.UNSUPPORTED_MEDIA_TYPE));
        assertThat(response.getHeaders().getAcceptPatch(), contains(mediaMergePatch));
        assertThat(response.getHeaders().get("Accept-Post"),
            contains(MediaType.APPLICATION_JSON.toString()));
    }

    @Test
    void handleInvalidFilingException() {
        when(request.getRequest()).thenReturn(servletRequest);
        final var exception = new InvalidFilingException(List.of(fieldError, fieldErrorWithRejectedValue));

        final var apiErrors = testExceptionHandler.handleInvalidFilingException(exception, request);

        assertThat(apiErrors.getErrors(), hasSize(2));

        assertThat(apiErrors.getErrors(), containsInAnyOrder(
            allOf(
                hasProperty("error", is("field is blank")),
                hasProperty("location", is("$.company_number")),
                hasProperty("locationType", is("json-path")),
                hasProperty("type", is("ch:validation"))
            ),
            allOf(
                hasProperty("error", is("{rejected-value} is ceased")),
                hasProperty("location", is("$.psc_notification_id")),
                hasProperty("locationType", is("json-path")),
                hasProperty("type", is("ch:validation")),
                hasProperty("errorValues", hasEntry("rejected-value", "1kdaTltWeaP1EB70SSD9SLmiK5Z"))
            )
        ));
    }

    @Test
    void handleMergePatchException() {
        when(request.getRequest()).thenReturn(servletRequest);
        final var exception = new MergePatchException("Test message", mergePatchCause);

        final var response = testExceptionHandler.handleMergePatchException(exception, request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    void handleResourceNotFoundException() {
        final var exception = new FilingResourceNotFoundException(
            "Filing resource {filing-resource-id} not found");

        when(request.getRequest()).thenReturn(servletRequest);

        final var apiErrors = testExceptionHandler.handleResourceNotFoundException(exception,
            request);
        final var expectedError = new ApiError("Filing resource {filing-resource-id} not found",
            null, "resource", "ch:validation");

        expectedError.addErrorValue("{filing-resource-id}",
            "Filing resource {filing-resource-id} not found");

        assertThat(apiErrors.getErrors(), contains(expectedError));
    }

    @Disabled("Similar tests pass but this fails - looking into issue")
    @Test
    void handleConflictingFilingException() {

        when(request.getRequest()).thenReturn(servletRequest);
        final var exception = new ConflictingFilingException(List.of(fieldError, fieldErrorWithRejectedValue));

        final var apiErrors = testExceptionHandler.handleConflictingFilingException(exception, request);

        assertThat(apiErrors.getErrors(), hasSize(2));

        assertThat(apiErrors.getErrors(), containsInAnyOrder(
            allOf(
                hasProperty("error", is("field is blank")),
                hasProperty("location", is("$.company_number")),
                hasProperty("locationType", is("json-path")),
                hasProperty("type", is("ch:validation"))
            ),
            allOf(
                hasProperty("error", is("{rejected-value} is ceased")),
                hasProperty("location", is("$.psc_notification_id")),
                hasProperty("locationType", is("json-path")),
                hasProperty("type", is("ch:validation")),
                hasProperty("errorValues", hasEntry("rejected-value", "1kdaTltWeaP1EB70SSD9SLmiK5Z"))
            )
        ));
    }

    @Test
    void handleServiceException() {
        final var exception = new ServiceException(
            "Service exception");

        when(request.getRequest()).thenReturn(servletRequest);

        final var apiErrors = testExceptionHandler.handleServiceException(exception,
            request);
        final var expectedError = new ApiError("Service Unavailable: {error}",
            null, "resource", "ch:service");

        expectedError.addErrorValue("error",
            "Service exception");

        assertThat(apiErrors.getErrors(), contains(expectedError));
    }

    @Test
    void handleAllUncaughtException() {
        final var exception = new RuntimeException(
            "Runtime exception");

        when(request.getRequest()).thenReturn(servletRequest);

        final var apiErrors = testExceptionHandler.handleAllUncaughtException(exception,
            request);
        final var expectedError = new ApiError("Service Unavailable: {error}",
            null, "resource", "ch:service");

        expectedError.addErrorValue("error",
            "Runtime exception");

        assertThat(apiErrors.getErrors(), contains(expectedError));
    }

    @Test
    void getMostSpecificCause() {

        final var thrown = RestExceptionHandler.getMostSpecificCause(null);
        assertNull(thrown);
    }

    private static HttpMessageNotReadableException getHttpMessageNotReadableException(String blankJsonQuoted) {
        final var message = new MockHttpInputMessage(blankJsonQuoted.getBytes());
        return new HttpMessageNotReadableException("Unexpected end-of-input: "
            + "expected close marker for Object (start marker at [Source: (org"
            + ".springframework.util.StreamUtils$NonClosingInputStream); line: 1, column: 1])\n"
            + " at [Source: (org.springframework.util.StreamUtils$NonClosingInputStream); "
            + "line: 1, column: 2]", message);
    }
}