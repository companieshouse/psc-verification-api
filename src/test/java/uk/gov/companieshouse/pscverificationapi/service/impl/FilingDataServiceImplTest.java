package uk.gov.companieshouse.pscverificationapi.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.model.pscverification.NameMismatchReasonConstants.PREFERRED_NAME;
import static uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants.INDIVIDUAL_VERIFIED;
import static uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants.RO_IDENTIFIED;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.psc.NameElementsApi;
import uk.gov.companieshouse.api.model.pscverification.InternalData;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.RelevantOfficer;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.config.FilingDataConfig;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.model.FilingKind;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.service.FilingDataService;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationService;

@ExtendWith(MockitoExtension.class)
class FilingDataServiceImplTest {
    private static final String TRANS_ID = "23445657412";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final String PSC_NOTIFICATION_ID = "abcdefgh";
    private static final String APPOINTMENT_ID = "12345678";
    private static final String UVID = "999999999";
    private static final String TITLE = "MR";
    private static final String FORENAME = "JOE";
    private static final String OTHER_FORENAMES = "TOM";
    private static final String SURNAME = "BLOGGS";
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(2022, 9, 13);
    public static final String PSC_VERIFICATION = "PSC Verification";

    @Mock
    private PscVerificationService pscVerificationService;
    @Mock
    private FilingDataConfig filingDataConfig;
    @Mock
    private Logger logger;
    private Transaction transaction;
    private FilingDataService testService;

    @BeforeEach
    void setUp() {
        testService = new FilingDataServiceImpl(pscVerificationService, filingDataConfig, logger);
        transaction = new Transaction();
        transaction.setId(TRANS_ID);
        transaction.setCompanyNumber(COMPANY_NUMBER);
    }

    @Test
    void generatePscVerificationIndividualFilingWhenFound() {
        final var verificationDetails = VerificationDetails.newBuilder()
                .uvid(UVID)
                .nameMismatchReason(PREFERRED_NAME)
                .statements(EnumSet.of(INDIVIDUAL_VERIFIED))
                .build();
        final var data = PscVerificationData.newBuilder()
                .companyNumber(COMPANY_NUMBER)
                .pscNotificationId(PSC_NOTIFICATION_ID)
                .verificationDetails(verificationDetails)
                .build();
        final var internalData = InternalData.newBuilder()
                .internalId(APPOINTMENT_ID)
                .build();
        final var filingData = PscVerification.newBuilder()
                .data(data)
                .internalData(internalData)
                .build();

        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.of(filingData));
        when(filingDataConfig.getPscVerificationDescription()).thenReturn(PSC_VERIFICATION);

        final var filingApi = testService.generateFilingApi(FILING_ID, transaction);

        final Map<String, Object> expectedMap;
        final String expectedDescription;

        expectedMap = Map.of("company_number", COMPANY_NUMBER,
                "appointment_id", APPOINTMENT_ID,
                "verification_details", Map.of("name_mismatch_reason", "PREFERRED_NAME",
                                                    "verification_statements", List.of("INDIVIDUAL_VERIFIED"),
                                                    "uvid", UVID));
        expectedDescription = PSC_VERIFICATION;
        assertThat(filingApi.getData(), is(equalTo(expectedMap)));
        assertThat(filingApi.getKind(), is(FilingKind.PSC_VERIFICATION_INDIVIDUAL.getValue()));
        assertThat(filingApi.getDescription(), is(expectedDescription));
    }

    @Test
    void generatePscIndividualFilingWhenNotFound() {
        when(pscVerificationService.get(FILING_ID)).thenReturn(Optional.empty());

        final var exception = assertThrows(FilingResourceNotFoundException.class,
                () -> testService.generateFilingApi(FILING_ID, transaction));

        assertThat(exception.getMessage(), is("PSC verification not found when generating filing for " + FILING_ID));
    }
}