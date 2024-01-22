package uk.gov.companieshouse.pscverificationapi.controller.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants.INDIVIDUAL_VERIFIED;
import static uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants.RO_DECLARATION;
import static uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants.RO_IDENTIFIED;
import static uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants.RO_VERIFIED;

import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.model.pscverification.PersonalDetails;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationLinks;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.model.mapper.PscVerificationMapperImpl;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;
import uk.gov.companieshouse.pscverificationapi.service.TransactionService;

@Tag("web")
@WebMvcTest(controllers = PscVerificationControllerImpl.class)
class PscVerificationControllerImplIT extends BaseControllerIT {
    private static final URI SELF_URI = URI.create(
        "/transactions/" + TRANS_ID + "/persons-with-significant-control-verification/" + FILING_ID);
    private static final URI VALIDATION_URI = URI.create(
        SELF_URI.toString() + "/validation_status");

    @MockBean
    private TransactionService transactionService;
    @MockBean
    private PscVerificationService pscVerificationService;
    @MockBean
    private MongoDatabaseFactory mongoDatabaseFactory;
    @SpyBean
    private PscVerificationMapperImpl filingMapper;
    @MockBean
    private Clock clock;
    @MockBean
    private Logger logger;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ApplicationContext context;

    private PscVerificationLinks links;

    public static Stream<Arguments> provideCreateVerificationData() {
        final var commonDto = PscVerificationData.newBuilder()
            .companyNumber(COMPANY_NUMBER)
            .pscAppointmentId(PSC_ID)
            .build();
        return Stream.of(Arguments.of(PscVerificationData.newBuilder(commonDto)
            .verificationDetails(INDIVIDUAL_DETAILS)
            .build(), false), Arguments.of(PscVerificationData.newBuilder(commonDto)
            .verificationDetails(RO_DETAILS)
            .relevantOfficerDetails(
                PersonalDetails.newBuilder().nameElements(NAME_ELEMENTS).build())
            .build(), true));
    }

    @BeforeEach
    void setUp() throws Exception {
        baseSetUp();
        links = PscVerificationLinks.newBuilder()
            .self(SELF_URI.toString())
            .validationStatus(VALIDATION_URI.toString())
            .build();
    }

    @ParameterizedTest(name = "[{index}] isRLE={1}")
    @MethodSource("provideCreateVerificationData")
    void createIndividualVerificationWhenPayloadOk(final PscVerificationData dto,
        final boolean isRLE) throws Exception {
        final var body = "{" + COMMON_FRAGMENT + (isRLE ? RLE_FRAGMENT + RO_FRAGMENT :
            INDIVIDUAL_FRAGMENT) + "}";
        final var entity = PscVerification.newBuilder()
            .createdAt(FIRST_INSTANT)
            .updatedAt(FIRST_INSTANT)
            .id(FILING_ID)
            .data(dto)
            .links(links)
            .build();

        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(
            transaction);
        when(pscVerificationService.save(any(PscVerification.class))).thenReturn(
                PscVerification.newBuilder(entity).id(FILING_ID).build())
            .thenAnswer(i -> PscVerification.newBuilder(i.getArgument(0)).build()
                // copy of first argument
            );
        when(clock.instant()).thenReturn(FIRST_INSTANT);

        final var expectedStatementNames = isRLE ? List.of(RO_IDENTIFIED.toString(),
            RO_DECLARATION.toString(), RO_VERIFIED.toString()).toArray() : List.of(
            INDIVIDUAL_VERIFIED.toString()).toArray();
        final var resultActions = mockMvc.perform(post(URL_PSC, TRANS_ID).content(body)
                .requestAttr("transaction", transaction)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", SELF_URI.toString()))
            .andExpect(jsonPath("$.created_at", is(FIRST_INSTANT.toString())))
            .andExpect(jsonPath("$.updated_at", is(FIRST_INSTANT.toString())))
            .andExpect(jsonPath("$.links.self", is(SELF_URI.toString())))
            .andExpect(jsonPath("$.links.validation_status", is(VALIDATION_URI.toString())))
            .andExpect(jsonPath("$.data.company_number", is(COMPANY_NUMBER)))
            .andExpect(jsonPath("$.data.psc_appointment_id", is(PSC_ID)))
            .andExpect(jsonPath("$.data.verification_details.uvid", is(UVID)))
            .andExpect(jsonPath("$.data.verification_details.verification_statements",
                containsInAnyOrder(expectedStatementNames)));
        if (isRLE) {
            resultActions
                .andExpect(jsonPath("$.data.relevant_officer_details.name_elements.title",
                is(NAME_ELEMENTS.getTitle())))
                .andExpect(jsonPath("$.data.relevant_officer_details.name_elements.forename",
                is(NAME_ELEMENTS.getForename())))
                .andExpect(jsonPath("$.data.relevant_officer_details.name_elements.other_forenames",
                is(NAME_ELEMENTS.getOtherForenames())))
                .andExpect(jsonPath("$.data.relevant_officer_details.name_elements.surname",
                is(NAME_ELEMENTS.getSurname())))
                ;
        }
        verify(filingMapper).toEntity(dto);
        verify(filingMapper).toApi(argThat((PscVerification v) -> FILING_ID.equals(v.getId())));
    }

}
