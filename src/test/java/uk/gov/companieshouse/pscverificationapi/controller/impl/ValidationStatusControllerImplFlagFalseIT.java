package uk.gov.companieshouse.pscverificationapi.controller.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.pscverificationapi.controller.impl.ValidationStatusControllerImpl.TRANSACTION_NOT_SUPPORTED_ERROR;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.mapper.ErrorMapper;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.model.mapper.PscVerificationMapper;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;
import uk.gov.companieshouse.pscverificationapi.service.TransactionService;

//Using Spring Web MVC
@Tag("web")
@WebMvcTest(controllers = ValidationStatusControllerImpl.class,
        properties = {"feature.flag.transactions.closable=false"})
class ValidationStatusControllerImplFlagFalseIT extends BaseControllerIT {

    @MockBean
    private TransactionService transactionService;
    @MockBean
    private PscVerificationService pscVerificationService;
    @MockBean
    private PscVerificationMapper filingMapper;
    @MockBean
    private ErrorMapper errorMapper;
    @MockBean
    private Logger logger;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        baseSetUp();
    }

    @Test
    void validateWhenFeatureFlagIsFalse() throws Exception {
        final var filing = PscVerification.newBuilder().id(PSC_ID).build();

        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.of(filing));

        mockMvc.perform(get(URL_PSC_VALIDATION_STATUS, TRANS_ID, FILING_ID).requestAttr("transaction", transaction)
                        .headers(httpHeaders)).andDo(print())
                //status code is '200' as this is expected behaviour
                .andExpect(status().isOk()).andExpect(jsonPath("$.is_valid", is(false)))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].error", is(TRANSACTION_NOT_SUPPORTED_ERROR)));
    }

}