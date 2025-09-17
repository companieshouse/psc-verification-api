package uk.gov.companieshouse.pscverificationapi.error;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.exception.ConflictingFilingException;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceInvalidException;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.exception.InvalidFilingException;
import uk.gov.companieshouse.pscverificationapi.exception.MergePatchException;
import uk.gov.companieshouse.pscverificationapi.exception.PscLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.exception.TransactionServiceException;

/**
 * Handle exceptions caused by client REST requests, propagated from Spring or the service
 * controllers.
 * <ul>
 *     <li>{@link HttpMessageNotReadableException}</li>
 *     <li>{@link InvalidFilingException}</li>
 *     <li>{@link FilingResourceNotFoundException}</li>
 *     <li>{@link FilingResourceInvalidException}</li>
 *     <li>{@link MergePatchException}</li>
 *     <li>{@link TransactionServiceException}</li>
 *     <li>{@link PscLookupServiceException}</li>
 *     <li>{@link ConflictingFilingException}</li>
 *     <li>{@link NoResourceFoundException}</li>
 *     <li>{@link HttpMediaTypeNotSupportedException}</li>
 *     <li>{@link RuntimeException}</li>
 *     <li>other internal exceptions</li>
 * </ul>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Pattern PARSE_MESSAGE_PATTERN = Pattern.compile("(Text .*)$",
        Pattern.MULTILINE);

    @Qualifier(value = "validation")
    protected Map<String, String> validation;
    private final Logger chLogger;

    @Autowired
    public RestExceptionHandler(final Map<String, String> validation, final Logger logger) {
        this.validation = validation;
        this.chLogger = logger;
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull final HttpMessageNotReadableException ex,
        @NonNull final HttpHeaders headers,
        @NonNull final HttpStatusCode status,
        @NonNull final WebRequest request) {
        return createRedactedErrorResponseEntity(ex, request, ex.getCause(), validation.get("json-syntax-prefix"));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(@NonNull final HttpMediaTypeNotSupportedException ex,
        @NonNull final HttpHeaders headers,
        @NonNull final HttpStatusCode status,
        @NonNull final WebRequest request) {
        logError(chLogger, request,
            String.format("Media type not supported: %s", ex.getContentType()), ex);

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .header(HttpHeaders.ACCEPT_PATCH, "application/merge-patch+json")
            .header("Accept-Post", "application/json")
            .build();
    }

    @ExceptionHandler(InvalidFilingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiErrors handleInvalidFilingException(final InvalidFilingException ex,
        final WebRequest request) {
        final var fieldErrors = ex.getFieldErrors();

        final List<ApiError> errorList = fieldErrors.stream()
            .map(e -> buildRequestBodyError(getFieldErrorApiEnumerationMessage(e),
                getJsonPath(e), e.getRejectedValue(), e.getField()))
            .toList();

        logError(chLogger, request, "Invalid filing data", ex, errorList);
        return new ApiErrors(errorList);
    }

    @ExceptionHandler(MergePatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<Object> handleMergePatchException(final MergePatchException ex,
        final WebRequest request) {
        logError(chLogger, request, "Invalid patch data", ex, null);
        return createRedactedErrorResponseEntity(ex, request, ex.getCause(),
            validation.get("patch-merge-error-prefix"));
    }

    @ExceptionHandler(ConflictingFilingException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ApiErrors handleConflictingFilingException(final ConflictingFilingException ex,
        final WebRequest request) {
        final var fieldErrors = ex.getFieldErrors();

        final var errorList = fieldErrors.stream()
            .map(e -> buildRequestBodyError(e.getDefaultMessage(), getJsonPath(e),
                e.getRejectedValue(), e.getField()))
            .toList();

        logError(chLogger, request, "Conflicting filing data", ex, errorList);
        return new ApiErrors(errorList);
    }

    @ExceptionHandler(FilingResourceNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public ApiErrors handleResourceNotFoundException(final FilingResourceNotFoundException ex,
        final WebRequest request) {
        final var error = new ApiError(validation.get("filing-resource-not-found"),
            getRequestURI(request),
            LocationType.RESOURCE.getValue(), ErrorType.VALIDATION.getType());

        Optional.ofNullable(ex.getMessage())
            .ifPresent(m -> error.addErrorValue("{filing-resource-id}", m));

        final var errorList = List.of(error);
        logError(chLogger, request, ex.getMessage(), ex, errorList);
        return new ApiErrors(errorList);
    }

    @ExceptionHandler(FilingResourceInvalidException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public ApiErrors handleResourceInvalidException(final FilingResourceInvalidException ex,
        final WebRequest request) {
        final var error = new ApiError(validation.get("psc-is-ceased"),
            getRequestURI(request),
            LocationType.RESOURCE.getValue(), ErrorType.VALIDATION.getType());

        Optional.ofNullable(ex.getMessage())
            .ifPresent(m -> error.addErrorValue("{filing-resource-id}", m));

        final var errorList = List.of(error);
        logError(chLogger, request, ex.getMessage(), ex, errorList);
        return new ApiErrors(errorList);
    }

    @ExceptionHandler({
        PscLookupServiceException.class,
        TransactionServiceException.class
    })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiErrors handleServiceException(final Exception ex,
        final WebRequest request) {
        final var errorList = List.of(createApiServiceError(ex, request, chLogger));
        logError(chLogger, request, ex.getMessage(), ex, errorList);
        return new ApiErrors(errorList);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(@NonNull final Exception ex,
        @Nullable final Object body,
        @NonNull final HttpHeaders headers,
        @NonNull final HttpStatusCode statusCode,
        @NonNull final WebRequest request) {
        List<ApiError> errorList = null;
        if (!(ex instanceof NoResourceFoundException)) {
            errorList = List.of(createApiServiceError(ex, request, chLogger));
            logError(chLogger, request, "INTERNAL ERROR", ex, errorList);
        }

        return super.handleExceptionInternal(ex, new ApiErrors(errorList), headers, statusCode, request);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiErrors handleAllUncaughtException(final RuntimeException ex,
        final WebRequest request) {
        final var errorList = List.of(createApiServiceError(ex, request, chLogger));
        logError(chLogger, request, "Internal error", ex, errorList);
        return new ApiErrors(errorList);
    }

    /**
     * Creates a generic API error for service-level exceptions, including error location and type.
     * Adds extra logging for certain IllegalArgumentExceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @param chLogger the logger
     * @return an ApiError representing the service error
     */
    private static ApiError createApiServiceError(final Exception ex,
        final WebRequest request,
        final Logger chLogger) {
        final var error = new ApiError("Service Unavailable: {error}",
            getRequestURI(request), LocationType.RESOURCE.getValue(),
            ErrorType.SERVICE.getType());

        Optional.ofNullable(ex)
            .filter(IllegalArgumentException.class::isInstance)
            .map(Throwable::getCause)
            .map(Throwable::getMessage)
            .map(m -> m.contains("expected numeric type"))
            .ifPresent(c -> logError(chLogger, request, "A dependent CHS service may be unavailable", ex));

        String errorMessage = "Internal server error";
        if (ex != null && ex.getMessage() != null && !ex.getMessage().trim().isEmpty()) {
            errorMessage = ex.getMessage();
        }

        error.addErrorValue("error", errorMessage);

        return error;
    }

    /**
     * Adds JSON location information (offset, line, column) to the given ApiError.
     *
     * @param error the ApiError to update
     * @param location the JSON location from a parsing exception
     */
    private static void addLocationInfo(final ApiError error, final JsonLocation location) {
        error.addErrorValue("offset", location.offsetDescription());
        error.addErrorValue("line", String.valueOf(location.getLineNr()));
        error.addErrorValue("column", String.valueOf(location.getColumnNr()));
    }

    /**
     * Derives a JSON path string from a FieldError's codes, for use in error reporting.
     *
     * @param e the FieldError
     * @return the JSON path string (e.g., $.fieldName)
     */
    private static String getJsonPath(final FieldError e) {
        return Optional.ofNullable(e.getCodes())
            .stream()
            .flatMap(Arrays::stream)
            .skip(1)
            .findFirst()
            .map(s -> s.replaceAll("^[^.]*", "\\$"))
            .map(s -> s.replaceAll("([A-Z0-9]+)", "_$1").toLowerCase())
            .orElse("$");
    }

    /**
     * Returns a user-friendly error message for mismatched input exceptions, such as unknown properties or parse errors.
     *
     * @param mismatchedInputException the exception
     * @return the error message string
     */
    private String getMismatchErrorMessage(
        final MismatchedInputException mismatchedInputException) {
        if (mismatchedInputException instanceof UnrecognizedPropertyException) {
            return validation.get("unknown-property-name");
        }
        else {
            final var message = mismatchedInputException.getMessage();
            final var parseMatcher = PARSE_MESSAGE_PATTERN.matcher(message);

            return parseMatcher.find() ? parseMatcher.group(1) : "";
        }

    }

    /**
     * Redacts sensitive or verbose error message content by truncating at the first colon.
     *
     * @param s the original error message
     * @return the redacted error message
     */
    private String redactErrorMessage(final String s) {
        return StringUtils.substringBefore(s, ":");
    }

    /**
     * Creates a ResponseEntity for a bad request, redacting sensitive error details and including JSON location info if available.
     * Handles both JSON parse errors and generic exceptions.
     *
     * @param ex the original exception
     * @param request the web request
     * @param cause the underlying cause (may be a JSON exception)
     * @param baseMessage the base error message prefix
     * @return a ResponseEntity with an ApiErrors body
     */
    private ResponseEntity<Object> createRedactedErrorResponseEntity(final RuntimeException ex,
        final WebRequest request, final Throwable cause, final String baseMessage) {
        final ApiError error;
        final String message;

        if (cause instanceof JsonProcessingException jpe) {
            final var location = jpe.getLocation();
            var jsonPath = "$";
            Object rejectedValue = null;

            if (cause instanceof MismatchedInputException mie) {
                message = getMismatchErrorMessage(mie);


                final var fieldNameOpt = ((MismatchedInputException) cause).getPath()
                    .stream()
                    .findFirst()
                    .map(JsonMappingException.Reference::getFieldName);
                jsonPath += fieldNameOpt.map(f -> "." + f)
                    .orElse("");

                if (jpe instanceof InvalidFormatException) {
                    rejectedValue = ((InvalidFormatException) cause).getValue();
                }
            }
            else {
                message = redactErrorMessage(cause.getMessage());
            }
            error = buildRequestBodyError(baseMessage + message, jsonPath, rejectedValue, null);
            addLocationInfo(error, location);

            if (cause instanceof UnrecognizedPropertyException unrecognized) {
                error.addErrorValue("property-name", unrecognized.getPropertyName());
            }
        }
        else {
            message = redactErrorMessage(getMostSpecificCause(ex).getMessage());
            error = buildRequestBodyError(baseMessage + message, "$", null, null);

        }
        logError(chLogger, request, String.format("Message not readable: %s", message), ex);
        return ResponseEntity.badRequest()
            .body(new ApiErrors(List.of(error)));
    }

    /**
     * Extracts the request URI from the WebRequest, or returns null if unavailable.
     *
     * @param request the web request
     * @return the request URI string, or null
     */
    private static String getRequestURI(final WebRequest request) {
        // resolveReference("request") preferred over getRequest() because the latter method is
        // final and cannot be stubbed with Mockito
        return Optional.ofNullable((HttpServletRequest) request.resolveReference("request"))
            .map(HttpServletRequest::getRequestURI)
            .orElse(null);
    }

    /**
     * Builds an ApiError for a request body validation error, including rejected value and property name if available.
     *
     * @param message the error message
     * @param jsonPath the JSON path to the error
     * @param rejectedValue the value that was rejected (may be null)
     * @param fieldName the field name (may be null)
     * @return an ApiError for the validation error
     */
    private static ApiError buildRequestBodyError(final String message, final String jsonPath,
        final Object rejectedValue, final String fieldName) {
         final var error = new ApiError(message, jsonPath, LocationType.JSON_PATH.getValue(),
            ErrorType.VALIDATION.getType());

        Optional.ofNullable(rejectedValue)
            .map(Object::toString)
            .filter(Predicate.not(String::isEmpty))
            .ifPresent(r -> error.addErrorValue("rejected-value", r));
        Optional.ofNullable(fieldName)
            .ifPresent(f -> error.addErrorValue("property-name", StringUtils.substringAfterLast(jsonPath, "$.")));

        return error;
    }

    /**
     * Logs an error with the given message and exception, using the provided logger and request context.
     *
     * @param chLogger the logger
     * @param request the web request
     * @param msg the log message
     * @param ex the exception
     */
    private static void logError(final Logger chLogger, final WebRequest request, final String msg,
        final Exception ex) {
        logError(chLogger, request, msg, ex, null);
    }

    /**
     * Logs an error with optional ApiError details, using the provided logger and request context.
     *
     * @param chLogger the logger
     * @param request the web request
     * @param msg the log message
     * @param ex the exception
     * @param apiErrorList optional list of ApiError objects for context
     */
    private static void logError(final Logger chLogger, final WebRequest request, final String msg,
        final Exception ex,
        @Nullable final List<ApiError> apiErrorList) {
        final Map<String, Object> logMap = new HashMap<>();
        final var servletRequest = ((ServletWebRequest) request).getRequest();
        logMap.put("path", servletRequest.getRequestURI());
        logMap.put("method", servletRequest.getMethod());
        Optional.ofNullable(apiErrorList).ifPresent(l -> logMap.put("errors", l));
        chLogger.error(msg, ex, logMap);
    }

    /**
     * Returns the most specific (root) cause of a throwable, or the original if no root cause exists.
     *
     * @param original the original throwable
     * @return the root cause or the original throwable
     */
    public static Throwable getMostSpecificCause(final Throwable original) {
        final Throwable rootCause = ExceptionUtils.getRootCause(original);

        return rootCause != null ? rootCause : original;
    }

    /**
     * Looks up the API error message for a FieldError using the validation map and error codes.
     *
     * @param e the FieldError
     * @return the API error message string
     */
    private String getFieldErrorApiEnumerationMessage(final FieldError e) {
        final var codes = Objects.requireNonNull(e.getCodes());
        return validation.get(codes[codes.length - 1]);
    }
}
