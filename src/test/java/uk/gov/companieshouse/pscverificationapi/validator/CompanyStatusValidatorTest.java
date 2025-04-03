package uk.gov.companieshouse.pscverificationapi.validator;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.service.CompanyProfileService;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyStatusValidatorTest {

    @Mock
    private CompanyProfileApi companyProfile;
    @Mock
    private CompanyProfileService companyProfileService;
    @Mock
    private Map<String, String> validation;
    @Mock
    private Map<String, List<String>> company;
    @Mock
    private PscVerificationData pscVerificationData;
    @Mock
    private Transaction transaction;

    CompanyStatusValidator testValidator;
    private PscType pscType;
    private Set<FieldError> errors;
    private String passthroughHeader;

    private static final List<String> companyStatusList = Collections.singletonList("dissolved");

    @BeforeEach
    void setUp() {

        errors = new HashSet<>();
        pscType = PscType.INDIVIDUAL;
        passthroughHeader = "passthroughHeader";

        testValidator = new CompanyStatusValidator(companyProfileService, validation, company);
    }

    @Test
    void validateWhenPscExists() {

        testValidator.validate(
            new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors, is(empty()));

    }

    @Test
    void validateWhenCompanyStatusNotAllowed() {

        when(companyProfileService.getCompanyProfile(transaction, pscVerificationData, passthroughHeader)).thenReturn(companyProfile);
        when(companyProfile.getCompanyStatus()).thenReturn("dissolved");
        when(company.get("status-not-allowed")).thenReturn(companyStatusList);
        when(validation.get("company-status-not-allowed")).thenReturn("status not allowed default message");

        var fieldError = new FieldError("object", "company_status", companyProfile.getCompanyStatus(), false,
            new String[]{null, "data.company_status"}, null, "status not allowed default message" + companyProfile.getCompanyStatus());

        testValidator.validate(
            new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors.stream().findFirst().orElseThrow(), equalTo(fieldError));
        assertThat(errors, IsIterableContainingInOrder.contains(fieldError));
    }
}