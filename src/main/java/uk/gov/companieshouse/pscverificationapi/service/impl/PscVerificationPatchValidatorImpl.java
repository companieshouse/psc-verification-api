package uk.gov.companieshouse.pscverificationapi.service.impl;

import static java.util.function.Predicate.not;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.AbstractBindingResult;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.SmartValidator;
import uk.gov.companieshouse.patch.model.ValidationResult;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;
import uk.gov.companieshouse.pscverificationapi.model.mapper.PscVerificationMapper;
import uk.gov.companieshouse.pscverificationapi.service.PscVerificationPatchValidator;

@Component
public class PscVerificationPatchValidatorImpl implements PscVerificationPatchValidator {

    private final SmartValidator validator;
    private final PscVerificationMapper mapper;

    @Autowired
    public PscVerificationPatchValidatorImpl(SmartValidator validator, PscVerificationMapper mapper) {
        this.validator = validator;
        this.mapper = mapper;
    }

    @Override
    public ValidationResult validate(PscVerification patchedFiling) {
        return Optional.ofNullable(patchedFiling)
                .map(mapper::toApi)
                .map(d -> {
                    final var e = new BeanPropertyBindingResult(d, "patched");
                    validator.validate(d, e);

                    return e;
                })
                .map(AbstractBindingResult::getFieldErrors)
                .filter(not(List::isEmpty))
                .map(ValidationResult::new)
                .orElseGet(ValidationResult::new);
    }
}
