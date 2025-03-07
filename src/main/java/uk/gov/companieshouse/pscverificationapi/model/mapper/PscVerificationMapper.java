package uk.gov.companieshouse.pscverificationapi.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
import uk.gov.companieshouse.pscverificationapi.model.PscVerificationDataForUpdating;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;

@Mapper(componentModel = "spring")
public interface PscVerificationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "links", ignore = true)
    @Mapping(target = "data", source = ".")
    PscVerification toEntity(final PscVerificationData data);

    @Mapping(target="companyNumber", source = "data.companyNumber")
    @Mapping(target="pscNotificationId", source = "data.pscNotificationId")
    @Mapping(target = "relevantOfficer", source = "data.relevantOfficer")
    @Mapping(target="verificationDetails", source = "data.verificationDetails")
    PscVerificationData toDto(final PscVerification verification);

    @Mapping(target = "etag", ignore = true)
    @Mapping(target = "kind", ignore = true)
    PscVerificationApi toApi(final PscVerification verification);

    PscVerificationDataForUpdating toForUpdating(final PscVerificationData data);

    @Mapping(target = "relevantOfficer.title", ignore = true)
    @Mapping(target = "relevantOfficer.forename", ignore = true)
    @Mapping(target = "relevantOfficer.otherForenames", ignore = true)
    @Mapping(target = "relevantOfficer.middleName", ignore = true)
    @Mapping(target = "relevantOfficer.surname", ignore = true)
    @Mapping(target = "verificationDetails.statements", source = "verificationDetails.verificationStatements")
    PscVerificationData fromForUpdating(final PscVerificationDataForUpdating pojo);

}

