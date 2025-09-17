package uk.gov.companieshouse.pscverificationapi.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;
import uk.gov.companieshouse.pscverificationapi.service.VerificationValidationService;
import uk.gov.companieshouse.pscverificationapi.validator.ValidationChainEnable;
import uk.gov.companieshouse.pscverificationapi.validator.VerificationValidationContext;

/**
 * The verification validation service layer that passes to the chain of validators
 * as defined in ValidatorConfig.
 * <p>
 * Implements {@link VerificationValidationService}
 * </p>
 */
@Service
public class VerificationValidationServiceImpl implements VerificationValidationService {

    private final Map<PscType, ? extends ValidationChainEnable> filingValidByPscType;

    @Autowired
    VerificationValidationServiceImpl(final List<? extends ValidationChainEnable> verificationValidators) {
        this.filingValidByPscType = verificationValidators.stream()
            .collect(Collectors.toMap(ValidationChainEnable::pscType, Function.identity()));
    }

    @Override
    public void validate(final VerificationValidationContext context) {
        Optional.ofNullable(filingValidByPscType.get(context.pscType()))
            .map(ValidationChainEnable::first)
            .ifPresentOrElse(v -> v.validate(context), () -> {
                throw new UnsupportedOperationException(
                    MessageFormat.format("Validation not defined for PSC type ''{0}''",
                        context.pscType()));
            });

    }
}
