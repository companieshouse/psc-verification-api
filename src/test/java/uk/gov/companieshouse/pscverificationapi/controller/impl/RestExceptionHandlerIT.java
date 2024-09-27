package uk.gov.companieshouse.pscverificationapi.controller.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.config.enumerations.PscVerificationConfig;
import uk.gov.companieshouse.pscverificationapi.controller.ValidationStatusController;
import uk.gov.companieshouse.pscverificationapi.service.FilingDataService;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;
import uk.gov.companieshouse.pscverificationapi.service.TransactionService;
import uk.gov.companieshouse.pscverificationapi.service.VerificationValidationService;

@Tag("web")
@WebMvcTest(controllers = FilingDataControllerImpl.class)
@Import(PscVerificationConfig.class)
class RestExceptionHandlerIT extends BaseControllerIT {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TransactionService transactionService;
    @MockBean
    private FilingDataService filingDataService;
    @MockBean
    private PscVerificationService pscVerificationService;
    @MockBean
    private VerificationValidationService validationService;
    @MockBean
    ValidationStatusController validationStatusController;
    @MockBean
    private Logger logger;
    @MockBean
    private ResourceHttpRequestHandler mockResourceHttpRequestHandler;

    @BeforeEach
    void setUp() throws Exception {
        baseSetUp();
    }

    @Override
    protected void setupEricTokenPermissions() {
        // don't add any ERIC permissions
    }

    @Test
    @DisplayName("handles transaction service unavailable error gracefully")
    void handleTransactionServiceUnavailable() throws Exception {
        final var body = "{" + COMMON_FRAGMENT + "}";

        // simulate exception caused by the transaction-api service unavailable
        // caused by JSON parse error in api-sdk-java
        final var cause = new IllegalArgumentException(
            "expected numeric type but got class uk.gov.companieshouse.api.error.ApiErrorResponse");

        when(transactionInterceptor.preHandle(any(), any(), any())).thenThrow(
            new IllegalArgumentException("", cause)); // message intentionally blank

        mockMvc.perform(post(URL_PSC, TRANS_ID).content(body)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(header().doesNotExist("location"))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].error", is("Service Unavailable: {error}")))
            .andExpect(
                jsonPath("$.errors[0].error_values", hasEntry("error", "Internal server error")))
            .andExpect(jsonPath("$.errors[0].type", is("ch:service")))
            .andExpect(jsonPath("$.errors[0].location_type", is("resource")));
    }

}
