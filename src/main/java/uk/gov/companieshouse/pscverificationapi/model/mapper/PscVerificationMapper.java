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
    @Mapping(target = "data", source = "data")
    @Mapping(target = "internalData", ignore = true)
    PscVerification toEntity(final PscVerificationData data);

    @Mapping(target="companyNumber", source = "data.companyNumber")
    @Mapping(target="pscNotificationId", source = "data.pscNotificationId")
    @Mapping(target="verificationDetails", source = "data.verificationDetails")
    PscVerificationData toDto(final PscVerification verification);

    @Mapping(target = "etag", ignore = true)
    @Mapping(target = "kind", ignore = true)
    @Mapping(target = "internalData", ignore = true)
    PscVerificationApi toApi(final PscVerification verification);

    @Mapping(target = "verificationDetails.verificationStatements", source = "verificationDetails.statements")
    PscVerificationDataForUpdating toForUpdating(final PscVerificationData data);

    @Mapping(target = "verificationDetails.statements", source = "verificationDetails.verificationStatements")
    PscVerificationData fromForUpdating(final PscVerificationDataForUpdating pojo);

}

