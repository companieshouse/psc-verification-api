package uk.gov.companieshouse.pscverificationapi.service;

import uk.gov.companieshouse.patch.service.PatchValidator;
import uk.gov.companieshouse.pscverificationapi.model.entity.PscVerification;

/**
 * Validator to ensure the Psc Verification patch contains valid data.
 */
public interface PscVerificationPatchValidator extends PatchValidator<PscVerification> {
}
