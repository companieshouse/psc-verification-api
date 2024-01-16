package uk.gov.companieshouse.pscverificationapi.controller.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationLinks;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.controller.PscVerificationController;

@ExtendWith(MockitoExtension.class)
class PscVerificationControllerImplTest {
    public static final String TRANS_ID = "117524-754816-491724";
    private static final URI REQUEST_URI = URI.create(
        "/transactions/" + TRANS_ID + "/persons-with-significant-control-verification");

    private PscVerificationController testController;

    @Mock
    private Logger logger;
    @Mock
    private BindingResult result;
    @Mock
    private PscVerificationData data;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Transaction transaction;

    @BeforeEach
    void setUp() {
//        testController = new PscVerificationControllerImpl(logger);
    }

    @Test
    void createPscVerification() {
        when(request.getRequestURI()).thenReturn(REQUEST_URI.toString());

        final var response = testController.createPscVerification(TRANS_ID, transaction, data, null,
            request);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));

        final var location = response.getHeaders().getLocation();
        final var resourceIdUri = REQUEST_URI.relativize(location);

        assertDoesNotThrow(() -> UUID.fromString(resourceIdUri.toString()));

        final var expected = PscVerificationApi.newBuilder().links(PscVerificationLinks.newBuilder()
            .self(location.toString())
            .validationStatus(UriComponentsBuilder.fromUri(location)
            .pathSegment("validation_status")
            .build().toString())
            .build()).build();

        assertThat(response.getBody(), is(equalTo(expected)));

    }
}
