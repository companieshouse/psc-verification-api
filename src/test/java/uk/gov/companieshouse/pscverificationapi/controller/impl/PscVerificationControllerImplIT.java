package uk.gov.companieshouse.pscverificationapi.controller.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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
import static uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants.RO_DECLARATION;
import static uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants.RO_IDENTIFIED;
import static uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants.RO_VERIFIED;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
import org.springframework.validation.Errors;
import uk.gov.companieshouse.api.model.common.ResourceLinks;
import uk.gov.companieshouse.api.model.psc.PscApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.RelevantOfficer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.error.RestExceptionHandler;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.model.mapper.PscVerificationMapperImpl;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;
import uk.gov.companieshouse.pscverificationapi.service.TransactionService;
import uk.gov.companieshouse.pscverificationapi.service.VerificationValidationService;

@Tag("web")
@WebMvcTest(controllers = PscVerificationControllerImpl.class)
//@Import(PscVerificationConfig.class)
//@EnableWebMvc
//@AutoConfigureMockMvc
//@ContextConfiguration(classes = {ValidatorConfig.class})
//@ComponentScan(basePackages = {"uk.gov.companieshouse.pscverificationapi.validator"})
class PscVerificationControllerImplIT extends BaseControllerIT {
    private static final URI SELF = URI.create(
        "/transactions/" + TRANS_ID + "/persons-with-significant-control-verification/" + FILING_ID);
    private static final URI VALID = URI.create(SELF.toString() + "/validation_status");
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1970, 1, 1);

    @MockBean
    private TransactionService transactionService;
    @MockBean
    private PscLookupService lookupService;
    @MockBean
    private PscVerificationService pscVerificationService;
    @MockBean
    private VerificationValidationService validationService;
    @MockBean
    private RestExceptionHandler restExceptionHandler;
    @MockBean
    private PscApi pscDetails;
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

    private ResourceLinks links;

    public static Stream<Arguments> provideCreateVerificationData() {
        final var commonDto = PscVerificationData.newBuilder()
            .companyNumber(COMPANY_NUMBER)
            .pscAppointmentId(PSC_ID)
            .build();

        return Stream.of(
            Arguments.of(PscVerificationData.newBuilder(commonDto)
            .verificationDetails(INDIVIDUAL_DETAILS)
            .build(), false),

            Arguments.of(PscVerificationData.newBuilder(commonDto)
            .verificationDetails(RO_DETAILS)
            .relevantOfficer(RelevantOfficer.newBuilder()
                .nameElements(NAME_ELEMENTS)
                .dateOfBirth(DATE_OF_BIRTH)
                .isDirector(true)
                .isEmployee(true)
                .build())
            .build(), true));
    }

    @BeforeEach
    void setUp() throws Exception {
        baseSetUp();
        links = ResourceLinks.newBuilder().self(SELF).validationStatus(VALID).build();
    }

    @ParameterizedTest(name = "[{index}] isRLE={1}")
    @MethodSource("provideCreateVerificationData")
    void createVerificationWhenPayloadOk(final PscVerificationData dto,
        final boolean isRLE) throws Exception {
        final var validationErrors = new ArrayList<Errors>();
        final var body = "{" + COMMON_FRAGMENT + (isRLE ? RLE_FRAGMENT + RO_FRAGMENT :
            INDIVIDUAL_FRAGMENT) + "}";
        final var entity = PscVerification.newBuilder()
            .createdAt(FIRST_INSTANT)
            .updatedAt(FIRST_INSTANT)
            .id(FILING_ID)
            .data(dto)
            .links(links)
            .build();

        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
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
            .andExpect(header().string("Location", SELF.toString()))
            .andExpect(jsonPath("$.created_at", is(FIRST_INSTANT.toString())))
            .andExpect(jsonPath("$.updated_at", is(FIRST_INSTANT.toString())))
            .andExpect(jsonPath("$.links.self", is(SELF.toString())))
            .andExpect(jsonPath("$.links.validation_status", is(VALID.toString())))
            .andExpect(jsonPath("$.data.company_number", is(COMPANY_NUMBER)))
            .andExpect(jsonPath("$.data.psc_appointment_id", is(PSC_ID)))
            .andExpect(jsonPath("$.data.verification_details.uvid", is(UVID)))
            .andExpect(jsonPath("$.data.verification_details.verification_statements",
                containsInAnyOrder(expectedStatementNames)));
        if (isRLE) {
            resultActions.andExpect(jsonPath("$.data.relevant_officer.name_elements.title",
                is(NAME_ELEMENTS.getTitle())))
                .andExpect(jsonPath("$.data.relevant_officer.name_elements.forename",
                is(NAME_ELEMENTS.getForename())))
                .andExpect(jsonPath("$.data.relevant_officer.name_elements.other_forenames",
                is(NAME_ELEMENTS.getOtherForenames())))
                .andExpect(jsonPath("$.data.relevant_officer.name_elements.surname",
                is(NAME_ELEMENTS.getSurname())))
                .andExpect(jsonPath("$.data.relevant_officer.date_of_birth",
                is(DATE_OF_BIRTH.toString())))
                .andExpect(jsonPath("$.data.relevant_officer.is_employee",
                is(true)))
                .andExpect(jsonPath("$.data.relevant_officer.is_director",
                is(true)))
                ;
        }
        verify(filingMapper).toEntity(dto);
        verify(filingMapper).toApi(argThat((PscVerification v) -> FILING_ID.equals(v.getId())));
    }

    //FIXME
    @Disabled
    @ParameterizedTest(name = "[{index}] isRLE={1}")
    @MethodSource("provideCreateVerificationData")
    void getPscVerificationThenResponse200(final PscVerificationData data,
                                           final boolean isRLE) throws Exception {

        final var expectedStatementNames = isRLE ? List.of(RO_IDENTIFIED.toString(),
                RO_DECLARATION.toString(), RO_VERIFIED.toString()).toArray() : List.of(
                INDIVIDUAL_VERIFIED.toString()).toArray();

        final var filing = PscVerification.newBuilder()
                .createdAt(FIRST_INSTANT)
                .updatedAt(SECOND_INSTANT)
                .links(links)
                .data(data)
                .build();

        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.of(filing));
        when(pscVerificationService.requestMatchesResourceSelf(any(HttpServletRequest.class), eq(filing))).thenReturn(true);

        final var resultActions = mockMvc.perform(get(URL_PSC_RESOURCE, TRANS_ID, FILING_ID).headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.created_at", is(FIRST_INSTANT.toString())))
            .andExpect(jsonPath("$.updated_at", is(SECOND_INSTANT.toString())))
            .andExpect(jsonPath("$.links.self", is(SELF.toString())))
            .andExpect(jsonPath("$.links.validation_status", is(VALID.toString())))
            .andExpect(jsonPath("$.data.company_number", is(COMPANY_NUMBER)))
            .andExpect(jsonPath("$.data.psc_appointment_id", is(PSC_ID)))
            .andExpect(jsonPath("$.data.verification_details.uvid", is(UVID)))
            .andExpect(jsonPath("$.data.verification_details.verification_statements",
                containsInAnyOrder(expectedStatementNames)));

        if (isRLE) {
            resultActions.andExpect(jsonPath("$.data.relevant_officer.name_elements.title",
                            is(NAME_ELEMENTS.getTitle())))
                .andExpect(jsonPath("$.data.relevant_officer.name_elements.forename",
                            is(NAME_ELEMENTS.getForename())))
                .andExpect(jsonPath("$.data.relevant_officer.name_elements.other_forenames",
                            is(NAME_ELEMENTS.getOtherForenames())))
                .andExpect(jsonPath("$.data.relevant_officer.name_elements.surname",
                            is(NAME_ELEMENTS.getSurname())))
                .andExpect(jsonPath("$.data.relevant_officer.date_of_birth",
                            is(DATE_OF_BIRTH.toString())))
                .andExpect(jsonPath("$.data.relevant_officer.is_employee",
                            is(true)))
                .andExpect(jsonPath("$.data.relevant_officer.is_director",
                            is(true)))
            ;
        }

    }

    //FIXME
    @Disabled
    @Test
    void getPscVerificationNotFoundThenResponse404() throws Exception {

        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get(URL_PSC_RESOURCE, TRANS_ID, FILING_ID).headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isNotFound());
        verifyNoInteractions(filingMapper);
    }
}
