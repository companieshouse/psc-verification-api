package uk.gov.companieshouse.pscverificationapi.validator;

import uk.gov.companieshouse.pscverificationapi.enumerations.PscType;

/**
 * Interface representing an entity that enables a validation chain.
 * Provides methods to retrieve the type of PSC (Person with Significant Control)
 * and the first validator in the chain.
 */
public interface ValidationChainEnable {

    PscType pscType();
    VerificationValidator first();
}
