package io.jenkins.blueocean.rest.factory.organization;

import io.jenkins.blueocean.rest.model.BlueOrganization;
import jenkins.model.ModifiableTopLevelItemGroup;

import javax.annotation.Nonnull;

/**
 * Base abstract class implementing {@link BlueOrganization}. Implementors of {@link BlueOrganization} should extend
 * from this class.
 *
 * @author Vivek Pandey
 * @see BlueOrganization
 * @see OrganizationFactory
 */
public abstract class AbstractOrganization extends BlueOrganization{

    /**
     * Gives the {@link ModifiableTopLevelItemGroup} associated with this organization
     */
    public @Nonnull abstract ModifiableTopLevelItemGroup getGroup();
}
