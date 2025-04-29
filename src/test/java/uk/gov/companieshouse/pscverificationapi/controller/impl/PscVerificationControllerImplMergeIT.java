package uk.gov.companieshouse.pscverificationapi.controller.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants.INDIVIDUAL_VERIFIED;

import java.net.URI;
import java.time.Clock;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.model.common.ResourceLinks;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.pscverificationapi.config.PatchServiceProperties;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.model.mapper.PscVerificationMapperImpl;
import uk.gov.companieshouse.pscverificationapi.repository.PscVerificationRepository;
import uk.gov.companieshouse.pscverificationapi.service.TransactionService;

@Tag("app")
@SpringBootTest
@ContextConfiguration(classes = {PscVerificationControllerImplMergeIT.SpringConfig.class})
@AutoConfigureMockMvc
class PscVerificationControllerImplMergeIT extends BaseControllerIT {
    private static final URI SELF = URI.create(
        "/transactions/" + TRANS_ID + "/persons-with-significant-control-verification/" + FILING_ID);
    private static final URI VALID = URI.create(SELF + "/validation_status");

    @MockitoBean
    private TransactionService transactionService;
    @MockitoBean
    private PscVerificationRepository repository;
    @MockitoBean
    private PatchServiceProperties patchServiceProperties;
    @MockitoSpyBean
    private PscVerificationMapperImpl filingMapper;
    @MockitoBean
    private MongoTransactionManager transactionManager;

    @TestConfiguration
    static class SpringConfig {
        @Bean
        Clock fixedClock() {
            return Clock.fixed(SECOND_INSTANT, ZoneId.of("UTC"));
        }
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ApplicationContext context;

    private ResourceLinks links;

    @BeforeEach
    void setUp() {
        baseSetUp();
        links = ResourceLinks.newBuilder().self(SELF).validationStatus(VALID).build();
        when(patchServiceProperties.getMaxRetries()).thenReturn(1);
    }

    @Test
    @DisplayName(
        "Expect to update or replace data fields that are present, skip any that are omitted")
    void updatePscVerificationWhenReplacingFields() throws Exception {
        final var body = new StringBuilder().append("{")
            .append("  \"company_number\": \"REPLACED\",")
            .append("\"verification_details\":{")
            .append("\"name_mismatch_reason\":\"LEGAL_NAME_CHANGE\",")
            .append("\"verification_statements\":[")
            .append("\"RO_IDENTIFIED\",")
            .append("\"RO_VERIFIED\",")
            .append("\"RO_DECLARATION\"]}")
            .append("}")
            .toString();
        final var dto = PscVerificationData.newBuilder()
            .companyNumber(COMPANY_NUMBER)
            .pscNotificationId(PSC_ID)
            .verificationDetails(VerificationDetails.newBuilder()
                .uvid(UVID)
                .statements(EnumSet.of(INDIVIDUAL_VERIFIED))
                .build())
            .build();
        final var filing = PscVerification.newBuilder()
            .id(FILING_ID)
            .createdAt(FIRST_INSTANT)
            .updatedAt(FIRST_INSTANT)
            .data(dto)
            .links(links)
            .build();
        final var expectedVerificationDetails = VerificationDetails.newBuilder(
                filing.getData().verificationDetails())
            .statements(EnumSet.of(INDIVIDUAL_VERIFIED))
            .uvid(UVID)
            .build();
        final var expectedPatchedDto = PscVerificationData.newBuilder(filing.getData())
            .companyNumber("REPLACED")
            .verificationDetails(expectedVerificationDetails)
            .build();
        final var expectedPatched = PscVerification.newBuilder(filing)
            .updatedAt(SECOND_INSTANT)
            .data(expectedPatchedDto)
            .build();

        when(repository.findById(FILING_ID)).thenReturn(Optional.of(filing))
            .thenReturn(Optional.of(expectedPatched));
        when(repository.save(any(PscVerification.class))).thenAnswer(
            i -> PscVerification.newBuilder(i.getArgument(0)).build()); // copy of first argument

        mockMvc.perform(patch(URL_PSC_RESOURCE, TRANS_ID, FILING_ID).content(body)
                .contentType(APPLICATION_JSON_MERGE_PATCH)
                .requestAttr("transaction", transaction)
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.created_at", is(FIRST_INSTANT.toString())))
            .andExpect(jsonPath("$.updated_at", is(SECOND_INSTANT.toString())))
            .andExpect(jsonPath("$.data.company_number", is("REPLACED")))
            .andExpect(jsonPath("$.data.psc_notification_id", is(PSC_ID)))
            .andExpect(jsonPath("$.data.verification_details.uvid", is(UVID)))
            .andExpect(jsonPath("$.data.verification_details.verification_statements",
                Matchers.containsInAnyOrder((INDIVIDUAL_VERIFIED.toString()))))
            .andExpect(header().stringValues("Location", links.self().toString()));
    }

    @Test
    @DisplayName("Expect to add nonexistent fields that are not read only, ignore any read only")
    void updatePscVerificationWhenAddingFields() throws Exception {
        final var body = new StringBuilder().append("{")
            .append("  \"company_number\": \"ADDED\",")
            .append("\"verification_details\":{")
            .append("\"name_mismatch_reason\":\"LEGAL_NAME_CHANGE\",")
            .append("\"verification_statements\":[")
            .append("\"INDIVIDUAL_VERIFIED\"]}")
            .append("}")
            .toString();
        final var dto = PscVerificationData.newBuilder()
            .pscNotificationId(PSC_ID)
            .build();
        final var filing = PscVerification.newBuilder()
            .id(FILING_ID)
            .createdAt(FIRST_INSTANT)
            .updatedAt(FIRST_INSTANT)
            .data(dto)
            .links(links)
            .build();
        final var expectedVerificationDetails = VerificationDetails.newBuilder()
            .statements(EnumSet.of(INDIVIDUAL_VERIFIED))
            .build();
        final var expectedPatchedDto = PscVerificationData.newBuilder(filing.getData())
            .companyNumber("ADDED")
            .verificationDetails(expectedVerificationDetails)
            .build();
        final var expectedPatched = PscVerification.newBuilder(filing)
            .updatedAt(SECOND_INSTANT)
            .data(expectedPatchedDto)
            .build();

        when(repository.findById(FILING_ID)).thenReturn(Optional.of(filing))
            .thenReturn(Optional.of(expectedPatched));
        when(repository.save(any(PscVerification.class))).thenAnswer(
            i -> PscVerification.newBuilder(i.getArgument(0)).build()); // copy of first argument

        mockMvc.perform(patch(URL_PSC_RESOURCE, TRANS_ID, FILING_ID).content(body)
                .contentType(APPLICATION_JSON_MERGE_PATCH)
                .requestAttr("transaction", transaction)
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.created_at", is(FIRST_INSTANT.toString())))
            .andExpect(jsonPath("$.updated_at", is(SECOND_INSTANT.toString())))
            .andExpect(jsonPath("$.data.company_number", is("ADDED")))
            .andExpect(jsonPath("$.data.psc_notification_id", is(PSC_ID)))
            .andExpect(jsonPath("$.data.verification_details.verification_statements",
                Matchers.containsInAnyOrder((INDIVIDUAL_VERIFIED.toString()))))
            .andExpect(header().stringValues("Location", links.self().toString()));
    }

    @Test
    @DisplayName(
        "Expect to delete existing fields that are not read only, ignore any read only or " +
            "nonexistent")
    void updatePscVerificationWhenDeletingFields() throws Exception {
        final var body = new StringBuilder().append("{")
            .append("  \"company_number\": null,")
            .append("\"verification_details\":{")
            .append("\"name_mismatch_reason\":\"LEGAL_NAME_CHANGE\",")
            .append("\"verification_statements\": null}")
            .append("}")
            .toString();
        final var dto = PscVerificationData.newBuilder()
            .companyNumber(COMPANY_NUMBER)
            .pscNotificationId(PSC_ID)
            .verificationDetails(VerificationDetails.newBuilder()
                .uvid(UVID)
                .statements(EnumSet.of(INDIVIDUAL_VERIFIED))
                .build())
            .build();
        final var filing = PscVerification.newBuilder()
            .id(FILING_ID)
            .createdAt(FIRST_INSTANT)
            .updatedAt(FIRST_INSTANT)
            .data(dto)
            .links(links)
            .build();
        final var expectedPatchedDto = PscVerificationData.newBuilder(filing.getData())
            .companyNumber(null)
            .verificationDetails(null)
            .build();
        final var expectedPatched = PscVerification.newBuilder(filing)
            .updatedAt(SECOND_INSTANT)
            .data(expectedPatchedDto)
            .build();

        when(repository.findById(FILING_ID)).thenReturn(Optional.of(filing))
            .thenReturn(Optional.of(expectedPatched));
        when(repository.save(any(PscVerification.class))).thenAnswer(
            i -> PscVerification.newBuilder(i.getArgument(0)).build()); // copy of first argument

        mockMvc.perform(patch(URL_PSC_RESOURCE, TRANS_ID, FILING_ID).content(body)
                .contentType(APPLICATION_JSON_MERGE_PATCH)
                .requestAttr("transaction", transaction)
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.created_at", is(FIRST_INSTANT.toString())))
            .andExpect(jsonPath("$.updated_at", is(SECOND_INSTANT.toString())))
            .andExpect(jsonPath("$.data.company_number").doesNotExist())
            .andExpect(jsonPath("$.data.psc_notification_id", is(PSC_ID)))
            .andExpect(
                jsonPath("$.data.verification_details.verification_statements").doesNotExist())
            .andExpect(header().stringValues("Location", links.self().toString()));
    }


    @Test
    @DisplayName("Expect updatedAt is updated even when no changes in the PATCH request ")
    void updatePscVerificationWhenFieldsAbsentThenTouchedButUnchanged() throws Exception {
        final var body = "{ }";
        final var dto = PscVerificationData.newBuilder()
            .companyNumber(COMPANY_NUMBER)
            .pscNotificationId(PSC_ID)
            .verificationDetails(VerificationDetails.newBuilder()
                .statements(EnumSet.of(INDIVIDUAL_VERIFIED))
                .build())
            .build();
        final var filing = PscVerification.newBuilder()
            .id(FILING_ID)
            .createdAt(FIRST_INSTANT)
            .updatedAt(FIRST_INSTANT)
            .data(dto)
            .links(links)
            .build();
        final var expectedPatched = PscVerification.newBuilder(filing)
            .updatedAt(SECOND_INSTANT)
            .build();

        when(repository.findById(FILING_ID)).thenReturn(Optional.of(filing))
            .thenReturn(Optional.of(expectedPatched));
        when(repository.save(any(PscVerification.class))).thenAnswer(
            i -> PscVerification.newBuilder(i.getArgument(0)).build()); // copy of first argument

        mockMvc.perform(patch(URL_PSC_RESOURCE, TRANS_ID, FILING_ID).content(body)
                .contentType(APPLICATION_JSON_MERGE_PATCH)
                .requestAttr("transaction", transaction)
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.created_at", is(FIRST_INSTANT.toString())))
            .andExpect(jsonPath("$.updated_at", is(SECOND_INSTANT.toString())))
            .andExpect(jsonPath("$.data.company_number", is(COMPANY_NUMBER)))
            .andExpect(jsonPath("$.data.psc_notification_id", is(PSC_ID)))
            .andExpect(jsonPath("$.data.verification_details.verification_statements",
                Matchers.containsInAnyOrder(INDIVIDUAL_VERIFIED.toString())))
            .andExpect(header().stringValues("Location", links.self().toString()));
    }

}
