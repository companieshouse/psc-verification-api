package uk.gov.companieshouse.pscverificationapi.controller.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.model.pscverification.VerificationStatementConstants.INDIVIDUAL_VERIFIED;

import java.time.Instant;
import java.util.EnumSet;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.psc.NameElementsApi;
import uk.gov.companieshouse.api.model.pscverification.VerificationDetails;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.api.util.security.Permission;

public class BaseControllerIT {
    protected static final String TRANS_ID = "4f56fdf78b357bfc";
    protected static final String FILING_ID = "632c8e65105b1b4a9f0d1f5e";
    protected static final String ETAG = "e7101610f832de81c8d2f27904d6b1de2be82ff6";
    protected static final String UVID = "999999999";
    protected static final String PASSTHROUGH_HEADER = "passthrough";
    protected static final String PSC_ID = "1kdaTltWeaP1EB70SSD9SLmiK5Y";
    protected static final String COMPANY_NUMBER = "012345678";
    protected static final String URL_PSC = "/transactions/{transactionId}/persons-with" +
        "-significant-control" + "-verification";
    protected static final String URL_PSC_RESOURCE = URL_PSC + "/{filingResourceId}";
    protected static final String URL_PSC_VALIDATION_STATUS = URL_PSC_RESOURCE + "/validation_status";
    protected static final String APPLICATION_JSON_MERGE_PATCH = "application/merge-patch+json";
    protected static final String COMMON_FRAGMENT = """
        "company_number": "%s",
        "psc_notification_id": "%s",
        "verification_details": {
            "uvid": "%s",
            "verification_statements": [
    """.formatted(COMPANY_NUMBER, PSC_ID, UVID);
    protected static final String INDIVIDUAL_FRAGMENT = """
        "INDIVIDUAL_VERIFIED"
            ]
        }
    """;
    protected static final String EMPTY_QUOTED_JSON = "\"\"";
    protected static final String MALFORMED_JSON = "{";
    protected static final Instant FIRST_INSTANT = Instant.parse("2022-10-15T09:44:08.108Z");
    protected static final Instant SECOND_INSTANT = Instant.parse("2023-01-15T09:33:52.900Z");
    protected static final VerificationDetails INDIVIDUAL_DETAILS = VerificationDetails.newBuilder()
        .uvid(UVID)
        .statements(EnumSet.of(INDIVIDUAL_VERIFIED))
        .build();
    protected static final NameElementsApi NAME_ELEMENTS = createNameElements("Sir", "Forename",
        "Other Forenames", "Surname");

    protected HttpHeaders httpHeaders;
    protected Transaction transaction;
    @MockitoBean
    protected TransactionInterceptor transactionInterceptor;

    void baseSetUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", PASSTHROUGH_HEADER);
        setupEricTokenPermissions();
        when(transactionInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        transaction = createOpenTransaction();
    }

    protected void setupEricTokenPermissions() {
        httpHeaders.add(EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS,
            Permission.Key.USER_PSC_VERIFICATION
                + "="
                + Permission.Value.CREATE
                + ","
                + Permission.Value.DELETE
                + ","
                + Permission.Value.READ);
    }

    protected Transaction createOpenTransaction() {
        transaction = new Transaction();
        transaction.setId(TRANS_ID);
        transaction.setStatus(TransactionStatus.OPEN);
        return transaction;
    }

    protected static NameElementsApi createNameElements(final String title, final String forename,
        final String otherForenames, final String surname) {
        final var nameElements = new NameElementsApi();

        nameElements.setTitle(title);
        nameElements.setForename(forename);
        nameElements.setOtherForenames(otherForenames);
        nameElements.setSurname(surname);

        return nameElements;
    }

}
