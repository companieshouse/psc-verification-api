package uk.gov.companieshouse.pscverificationapi.controller.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.config.IntegrationTestConfig;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;
import uk.gov.companieshouse.pscverificationapi.service.TransactionService;

//Using Spring Web MVC
@Tag("app")
@WebMvcTest(controllers = ValidationStatusControllerImpl.class,
        properties = {"feature.flag.transactions.closable=true"})
@ContextConfiguration(classes = {IntegrationTestConfig.class})
class ValidationStatusControllerImplFlagTrueIT extends BaseControllerIT {

    @MockBean
    private TransactionService transactionService;
    @MockBean
    private PscVerificationService pscVerificationService;
    @MockBean
    private Logger logger;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        baseSetUp();
    }

    @Test
    void validateWhenFilingNotFound() throws Exception {
        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get(URL_PSC_VALIDATION_STATUS, TRANS_ID, FILING_ID)
                .requestAttr("transaction", transaction)
                .headers(httpHeaders))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].error", is("Filing resource {filing-resource-id} not found")));
    }

}