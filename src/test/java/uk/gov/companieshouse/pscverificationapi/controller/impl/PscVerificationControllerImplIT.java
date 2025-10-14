package uk.gov.companieshouse.pscverificationapi.controller.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants.INDIVIDUAL_VERIFIED;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.companieshouse.api.interceptor.OpenTransactionInterceptor;
import uk.gov.companieshouse.api.model.common.ResourceLinks;
import uk.gov.companieshouse.api.model.psc.PscApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.psc.IdentityVerificationDetails;
import uk.gov.companieshouse.api.psc.IndividualFullRecord;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.config.IntegrationTestConfig;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.error.RestExceptionHandler;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.model.mapper.PscVerificationMapper;
import uk.gov.companieshouse.pscverificationapi.service.CompanyProfileService;
import uk.gov.companieshouse.pscverificationapi.service.IdvLookupService;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;
import uk.gov.companieshouse.pscverificationapi.service.TransactionService;
import uk.gov.companieshouse.pscverificationapi.service.VerificationValidationService;

@Tag("web")
@Import({IntegrationTestConfig.class})
@WebMvcTest(controllers = PscVerificationControllerImpl.class)
class PscVerificationControllerImplIT extends BaseControllerIT {
    private static final URI SELF = URI.create(
        "/transactions/" + TRANS_ID + "/persons-with-significant-control-verification/" + FILING_ID);
    private static final URI VALID = URI.create(SELF + "/validation_status");

    @MockitoBean
    private TransactionService transactionService;
    @MockitoBean
    private PscLookupService lookupService;
    @MockitoBean
    private PscVerificationService pscVerificationService;
    @MockitoBean
    private CompanyProfileService companyProfileService;
    @MockitoBean
    private IdvLookupService idvLookupService;
    @MockitoBean
    private VerificationValidationService validationService;
    @Autowired
    @Qualifier("validation")
    private Map<String, String> validation;
    @MockitoBean
    private PscApi pscDetails;
    @MockitoBean
    private IndividualFullRecord individualFullRecord;
    @MockitoBean
    private MongoDatabaseFactory mongoDatabaseFactory;
    @MockitoSpyBean
    private PscVerificationMapper filingMapper;
    @MockitoBean
    protected OpenTransactionInterceptor openTransactionInterceptor;
    @MockitoBean
    private Clock clock;
    @MockitoBean
    private Logger logger;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RestExceptionHandler restExceptionHandler;

    @Autowired
    private WebApplicationContext context;

    private ResourceLinks links;
    private PscVerificationData completeDto;
    private String individualPayload;
    private PscVerification entity;
    private IdentityVerificationDetails idvDetails;

    @BeforeEach
    void setUp() {
        baseSetUp();
        links = ResourceLinks.newBuilder().self(SELF).validationStatus(VALID).build();
        completeDto = PscVerificationData.newBuilder()
            .companyNumber(COMPANY_NUMBER)
            .pscNotificationId(PSC_ID)
            .verificationDetails(INDIVIDUAL_DETAILS)
            .build();
        entity = PscVerification.newBuilder()
            .createdAt(FIRST_INSTANT)
            .updatedAt(FIRST_INSTANT)
            .id(FILING_ID)
            .data(completeDto)
            .links(links)
            .build();
        individualPayload = "{" + COMMON_FRAGMENT + INDIVIDUAL_FRAGMENT + "}";
        idvDetails = new IdentityVerificationDetails()
                .appointmentVerificationStatementDate(LocalDate.now().minusDays(7))
                .appointmentVerificationStatementDueOn(LocalDate.now().plusDays(7));
        when(openTransactionInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void createVerificationWhenPayloadOk() throws Exception {
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(individualFullRecord.getInternalId()).thenReturn(Long.valueOf("123"));
        when(individualFullRecord.getIdentityVerificationDetails()).thenReturn(idvDetails);
        when(lookupService.getIndividualFullRecord(transaction, completeDto, PscType.INDIVIDUAL)).thenReturn(
            individualFullRecord);
        when(pscVerificationService.save(any(PscVerification.class))).thenReturn(
                PscVerification.newBuilder(entity).id(FILING_ID).build())
            .thenAnswer(i -> PscVerification.newBuilder(i.getArgument(0)).build()
                // copy of first argument
            );
        when(clock.instant()).thenReturn(FIRST_INSTANT);

        mockMvc.perform(post(URL_PSC, TRANS_ID).content(individualPayload)
                .requestAttr("transaction", transaction)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", SELF.toString()))
            .andExpect(jsonPath("$.created_at", is(FIRST_INSTANT.toString())))
            .andExpect(jsonPath("$.updated_at", is(FIRST_INSTANT.toString())))
            .andExpect(jsonPath("$.links.self", is(SELF.toString())))
            .andExpect(jsonPath("$.links.validation_status", is(VALID.toString())))
            .andExpect(jsonPath("$.data.company_number", is(COMPANY_NUMBER)))
            .andExpect(jsonPath("$.data.psc_notification_id", is(PSC_ID)))
            .andExpect(jsonPath("$.data.verification_details.uvid", is(UVID)))
            .andExpect(jsonPath("$.data.verification_details.verification_statements[0]",
                is(INDIVIDUAL_VERIFIED.toString())));
        verify(filingMapper).toEntity(completeDto);
        verify(filingMapper).toApi(argThat((PscVerification v) -> FILING_ID.equals(v.getId())));
        verify(transactionService).updateTransaction(transaction, PASSTHROUGH_HEADER);
    }

    @Test
    void createVerificationWhenMissingRequiredFields() throws Exception {
        final var incompleteFragment = """
                "company_number": "%s",
                "verification_details": {
                    "uvid": "%s",
                    "verification_statements": [
            """.formatted(COMPANY_NUMBER, UVID);
        individualPayload = "{" + incompleteFragment + INDIVIDUAL_FRAGMENT + "}";

        mockMvc.perform(post(URL_PSC, TRANS_ID).content(individualPayload)
                .requestAttr("transaction", transaction)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].error", is("Property {property-name} is required and must not be blank")))
            .andExpect(jsonPath("$.errors[0].error_values.property-name", is("psc_notification_id")))
            .andExpect(jsonPath("$.errors[0].location", is("$.psc_notification_id")))
            .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
            .andExpect(jsonPath("$.errors[0].type", is("ch:validation")));
        verifyNoInteractions(transactionService, pscVerificationService, clock, filingMapper);
    }

    @Test
    void getPscVerificationNotFoundThenResponse404() throws Exception {
        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get(URL_PSC_RESOURCE, TRANS_ID, FILING_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isNotFound());
        verifyNoInteractions(filingMapper, transactionService);
    }

}
