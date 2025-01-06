package uk.gov.companieshouse.pscverificationapi.service.impl;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.identityverification.model.UvidMatch;
import uk.gov.companieshouse.api.model.psc.IndividualFullRecord;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.psc.DateOfBirth;
import uk.gov.companieshouse.api.psc.NameElements;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.exception.FilingResourceNotFoundException;
import uk.gov.companieshouse.pscverificationapi.exception.PscLookupServiceException;
import uk.gov.companieshouse.pscverificationapi.helper.JsonHelper;
import uk.gov.companieshouse.pscverificationapi.sdk.companieshouse.ApiClientService;
import uk.gov.companieshouse.pscverificationapi.service.PscLookupService;
import uk.gov.companieshouse.pscverificationapi.utils.LogHelper;

@Service
public class PscLookupServiceImpl implements PscLookupService {
    private static final String UNEXPECTED_STATUS_CODE = "Unexpected Status Code received";

    private final ApiClientService apiClientService;
    private final Logger logger;

    public PscLookupServiceImpl(ApiClientService apiClientService, Logger logger) {
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    /**
     * Retrieve a PSC by PscVerificationData.
     *
     * @param transaction           the Transaction
     * @param data                  the PSC verification data
     * @param pscType               the PSC Type
     * @return the PSC Full Record details, if found
     * @throws PscLookupServiceException if the PSC was not found or an error occurred
     */
    @Override
    public IndividualFullRecord getPscIndividualFullRecord(final Transaction transaction, final PscVerificationData data,
                                                           final PscType pscType)
            throws PscLookupServiceException {

        final var logMap = LogHelper.createLogMap(transaction.getId());
        String pscAppointmentId = data.pscAppointmentId();
        EnvironmentReader environmentReader = new EnvironmentReaderImpl();
        String chsInternalApiKey = environmentReader.getMandatoryString("CHS_INTERNAL_API_KEY");

        try {
            final var uri = "/company/"
                    + data.companyNumber()
                    + "/persons-with-significant-control/"
                    + pscType.getValue()
                    + "/"
                    + pscAppointmentId
                    + "/full_record";

            return apiClientService.getApiClient(chsInternalApiKey)
                .pscs()
                .getIndividualFullRecord(uri)
                .execute()
                .getData();


        } catch (final ApiErrorResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
                logger.errorContext(transaction.getId(), UNEXPECTED_STATUS_CODE, e, logMap);
                throw new FilingResourceNotFoundException(
                        MessageFormat.format("PSC Details not found for {0}: {1} {2}", pscAppointmentId,
                                e.getStatusCode(), e.getStatusMessage()), e);
            }
            throw new PscLookupServiceException(
                    MessageFormat.format("Error Retrieving PSC details for {0}: {1} {2}", pscAppointmentId,
                            e.getStatusCode(), e.getStatusMessage()), e);

        } catch (URIValidationException e) {
            logger.errorContext(transaction.getId(), UNEXPECTED_STATUS_CODE, e, logMap);
            throw new PscLookupServiceException(
                    MessageFormat.format("Error Retrieving PSC details for {0}: {1}", pscAppointmentId,
                            e.getMessage()), e);
        }
    }

    /**
     * Retrieve a UvidMatch with the PSC data.
     *
     * @param transaction       the Transaction
     * @param data              the PSC verification data
     * @param pscType           the PSC Type
     * @return the UvidMatch, if found
     * @throws PscLookupServiceException if the PSC was not found or an error occurred
     */
    @Override
    public UvidMatch getUvidMatchWithPscData(final Transaction transaction,
        final PscVerificationData data, final PscType pscType)
        throws PscLookupServiceException {

        UvidMatch uvidMatch = new UvidMatch();
        Optional<String> uvid = Optional.ofNullable(data.verificationDetails().uvid());
        uvidMatch.setUvid(uvid.orElse(""));

        IndividualFullRecord pscIndividualFullRecord = getPscIndividualFullRecord(transaction, data, pscType);
        setUvidDataFromPsc(uvidMatch, pscIndividualFullRecord);

        return uvidMatch;
    }

    private void setUvidDataFromPsc(UvidMatch uvidMatch, IndividualFullRecord pscData) {

        NameElements convertedNameElements = JsonHelper.convertLinkedHashmap(pscData.getNameElements(), PropertyNamingStrategies.SNAKE_CASE, NameElements.class);
        List<String> forenames = new ArrayList<>();

        String forename = convertedNameElements.getForename();
        String middleNames = convertedNameElements.getMiddleName();
        String surname = convertedNameElements.getSurname();

        if (forename != null) {
            forenames.add(forename);
        }
        if (middleNames != null) {
            forenames.add(middleNames);
        }
        uvidMatch.setForenames(forenames);
        uvidMatch.setSurname(surname);

        DateOfBirth dateOfBirth = JsonHelper.convertLinkedHashmap(pscData.getDateOfBirth(), PropertyNamingStrategies.SNAKE_CASE, DateOfBirth.class);
        String stringDateOfBirth = String.format("%04d-%02d-%02d", dateOfBirth.getYear(), dateOfBirth.getMonth(), dateOfBirth.getDay());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        uvidMatch.setDateOfBirth(LocalDate.parse(stringDateOfBirth, formatter));
    }
}
