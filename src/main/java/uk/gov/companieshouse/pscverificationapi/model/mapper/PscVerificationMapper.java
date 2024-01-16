package uk.gov.companieshouse.pscverificationapi.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationApi;
import uk.gov.companieshouse.api.model.pscverification.PscVerificationData;
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
    @Mapping(target="pscAppointmentId", source = "data.pscAppointmentId")
    @Mapping(target="personalDetails", source = "data.personalDetails")
    @Mapping(target="verificationDetails", source = "data.verificationDetails")
    PscVerificationData toDto(final PscVerification verification);

    @Mapping(target = "etag", ignore = true)
    @Mapping(target = "kind", ignore = true)
    PscVerificationApi toApi(final PscVerification verification);
}

