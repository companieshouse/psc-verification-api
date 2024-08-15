package uk.gov.companieshouse.pscverificationapi.interceptor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.AttributeName;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.exception.ConflictingFilingException;
import uk.gov.companieshouse.pscverificationapi.service.CompanyProfileService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@ExtendWith(MockitoExtension.class)
class CompanyInterceptorTest {

    private static final String PASSTHROUGH_HEADER = "passthrough";
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Object handler;
    @Mock
    private CompanyProfileService companyProfileService;
    @Mock
    private PscVerificationData pscVerificationData;
    @Mock
    Logger logger;
    @Mock
    private Transaction transaction;
    @Mock
    private Map<String, String> validation;
    @Mock
    private  Map<String, List<String>> company;

    private CompanyProfileApi companyProfileApi;
    private CompanyInterceptor testCompanyInterceptor;

    @BeforeEach
    void setUp() {
        companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setHasSuperSecurePscs(Boolean.FALSE);
        companyProfileApi.setType("ltd");
        companyProfileApi.setCompanyStatus("active");

        testCompanyInterceptor =new CompanyInterceptor(companyProfileService, validation, company, logger);

        when(request.getAttribute(AttributeName.TRANSACTION.getValue())).thenReturn(transaction);
    }

    @Test
    void preHandleNoValidationErrors() throws IOException {
        expectHeaderWithCompanyProfile();
        when(company.get("type-allowed")).thenReturn(List.of("ltd"));

        var result = testCompanyInterceptor.preHandle(request, response, handler);

        assertTrue(result);
    }

    @Test
    void preHandleWhenCompanyTypeNotAllowed() {
        expectHeaderWithCompanyProfile();
        companyProfileApi.setType("not-proper");
        when(company.get("type-allowed")).thenReturn(List.of("ltd"));
        when(validation.get("company-type-not-allowed")).thenReturn("Invalid type default message");
        final var error = new FieldError("ignored", "ignored", "Invalid type default message");
        List<FieldError> errors = List.of(error);

        final var thrown = assertThrows(ConflictingFilingException.class,
            () -> testCompanyInterceptor.preHandle(request, response, handler));

        assertThat(thrown.getFieldErrors(), is(errors));
    }

    @Test
    void preHandleWhenCompanyProfileNull() throws Exception {
        expectHeaderWithCompanyProfile();
        when(companyProfileService.getCompanyProfile(transaction, pscVerificationData, PASSTHROUGH_HEADER)).thenReturn(null);
        var result = testCompanyInterceptor.preHandle(request, response, handler);
        assertTrue(result);
    }

    @Test
    void preHandleWhenTransactionInRequestNull() {
        when(request.getAttribute(AttributeName.TRANSACTION.getValue())).thenReturn(null);
        final var thrown = assertThrows(NullPointerException.class,
            () -> testCompanyInterceptor.preHandle(request, response, handler));
        assertThat(thrown.getMessage(), is("Transaction missing from request"));
    }

    private void expectHeaderWithCompanyProfile() {
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(companyProfileService.getCompanyProfile(transaction, pscVerificationData, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);
    }
}

