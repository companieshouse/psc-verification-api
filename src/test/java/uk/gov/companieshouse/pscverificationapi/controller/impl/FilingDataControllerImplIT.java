package uk.gov.companieshouse.pscverificationapi.controller.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.AttributeName;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.config.enumerations.PscVerificationConfig;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.model.FilingKind;
import uk.gov.companieshouse.pscverificationapi.service.FilingDataService;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;
import uk.gov.companieshouse.pscverificationapi.service.TransactionService;

@Tag("web")
@WebMvcTest(controllers = FilingDataControllerImpl.class)
@Import(PscVerificationConfig.class)
class FilingDataControllerImplIT extends BaseControllerIT {
    @MockBean
    private FilingDataService filingDataService;
    @MockBean
    private PscVerificationService pscVerificationService;
    @MockBean
    private Logger logger;
    @MockBean
    private TransactionService transactionService;
    @MockBean
    private AuthorisationUtil authorisationUtil;
    @MockBean
    private HttpServletRequest request;
    @Autowired
    private MockMvc mockMvc;

    private static final String URL_PSC_VERIFICATION =
            "/private/transactions/{id}/persons-with-significant-control-verification";
    private static final String FILINGS_SUFFIX = "/{filingId" + "}/filings";

    @BeforeEach
    void setUp() throws Exception {
        baseSetUp();
    }

    @Test
    void getFilingsWhenFound() throws Exception {
        final Map<String, Object> dataMap = Map.of("company_number", COMPANY_NUMBER,
                "psc_appointment_id", PSC_ID,
                "verification_details", Map.of("name_mismatch_reason", "PREFERRED_NAME",
                        "verification_statements", List.of("INDIVIDUAL_VERIFIED"),
                        "uvid", UVID));

        final var filingApi = new FilingApi();
        filingApi.setKind(FilingKind.PSC_VERIFICATION_INDIVIDUAL.getValue());
        filingApi.setData(dataMap);

        transaction.setStatus(TransactionStatus.CLOSED);
        when(filingDataService.generateFilingApi(FILING_ID, transaction)).thenReturn(filingApi);

        mockMvc.perform(get(URL_PSC_VERIFICATION + FILINGS_SUFFIX, TRANS_ID, FILING_ID).headers(httpHeaders)
                .requestAttr(AttributeName.TRANSACTION.getValue(), transaction)
                .header("ERIC-Identity", "abcdefg")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Roles", "*"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].kind", is(FilingKind.PSC_VERIFICATION_INDIVIDUAL.getValue())));
    }

    @Test
    void getFilingsWhenNotFoundAndTransactionNull() throws Exception {

        mockMvc.perform(get(URL_PSC_VERIFICATION + FILINGS_SUFFIX, TRANS_ID, FILING_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void getFilingsWhenNotFound() throws Exception {
        transaction.setStatus(TransactionStatus.CLOSED);

        when(filingDataService.generateFilingApi(FILING_ID, transaction)).thenThrow(
                new FilingResourceNotFoundException("for Not Found scenario", null));

        mockMvc.perform(get(URL_PSC_VERIFICATION + FILINGS_SUFFIX, TRANS_ID, FILING_ID).headers(httpHeaders)
                        .requestAttr(AttributeName.TRANSACTION.getValue(), transaction)
                .header("ERIC-Identity", "abcdefg")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Roles", "*"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].error", is("Filing resource {filing-resource-id} not found")))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("resource")));
    }

    @ParameterizedTest
    @EnumSource(value = TransactionStatus.class, names = {"CLOSED"}, mode = EnumSource.Mode.EXCLUDE)
    void getFilingsWhenFoundAndTransactionNotClosed(TransactionStatus transactionStatus) throws Exception {

        transaction.setStatus(transactionStatus);
        final var filingApi = new FilingApi();
        filingApi.setKind(FilingKind.PSC_VERIFICATION_INDIVIDUAL.getValue());

        when(filingDataService.generateFilingApi(FILING_ID, transaction)).thenReturn(filingApi);

        mockMvc.perform(get(URL_PSC_VERIFICATION + FILINGS_SUFFIX, TRANS_ID, FILING_ID).headers(httpHeaders)
                        .requestAttr(AttributeName.TRANSACTION.getValue(), transaction))
                .andExpect(status().isForbidden());
    }
}