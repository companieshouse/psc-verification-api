package uk.gov.companieshouse.pscverificationapi.mapper;

import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.pscverificationapi.error.ErrorType;
import uk.gov.companieshouse.pscverificationapi.error.LocationType;

/**
 * Mapper for converting {@link FieldError} objects to {@link ValidationStatusError}.
 * <p>
 * Uses MapStruct to map validation errors.
 * </p>
 */
@Mapper(componentModel = "spring", imports = {ErrorType.class, LocationType.class})
public interface ErrorMapper {
    @Mapping(target = "error", source = "defaultMessage")
    @Mapping(target="type", expression = "java(ErrorType.VALIDATION.getType())")
    @Mapping(target="location", expression = "java(\"$.\" + fieldError.getField())")
    @Mapping(target="locationType", expression = "java(LocationType.JSON_PATH.getValue())")
    ValidationStatusError map(final FieldError fieldError);

    ValidationStatusError[] map(final Set<FieldError> fieldErrors);
}
