package uk.gov.companieshouse.pscverificationapi.validator;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.jupiter.api.AfterEach;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyTypeValidatorTest {

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

    CompanyTypeValidator testValidator;
    private PscType pscType;
    private List<FieldError> errors;
    private String passthroughHeader;

    private static final List<String> companyTypeList = new ArrayList<>();

    @BeforeEach
    void setUp() {

        errors = new ArrayList<>();
        pscType = PscType.INDIVIDUAL;
        passthroughHeader = "passthroughHeader";

        testValidator = new CompanyTypeValidator(companyProfileService, validation, company);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void validateWhenPscExists() {

        testValidator.validate(
            new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors, is(empty()));

    }

    @Test
    void validateWhenCompanyTypeNotAllowed() {

        var fieldError = new FieldError("object", "type", companyProfile.getType(), false,
            new String[]{null, "data.type"}, null, "type not allowed default message");

        when(companyProfileService.getCompanyProfile(transaction, pscVerificationData, passthroughHeader)).thenReturn(companyProfile);
        when(companyProfile.getType()).thenReturn(null);
        when(company.get("type-allowed")).thenReturn(companyTypeList);
        when(validation.get("company-type-not-allowed")).thenReturn("type not allowed default message");

        testValidator.validate(
            new VerificationValidationContext(pscVerificationData, errors, transaction, pscType, passthroughHeader));

        assertThat(errors.stream().findFirst().orElseThrow(), equalTo(fieldError));
        assertThat(errors, IsIterableContainingInOrder.contains(fieldError));
    }
}