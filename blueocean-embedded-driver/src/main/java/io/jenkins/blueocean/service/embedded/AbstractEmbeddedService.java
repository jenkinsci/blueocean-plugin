package io.jenkins.blueocean.service.embedded;

import io.jenkins.blueocean.commons.ServiceException;
import jenkins.model.Jenkins;

/**
 * Abstract class, all embedded API implementations must extend this.
 *
 * @author Vivek Pandey
 */
public abstract class AbstractEmbeddedService {

    protected final Jenkins jenkins = Jenkins.getActiveInstance();

    protected void validateOrganization(String organization){
        if (!organization.equals(Jenkins.getActiveInstance().getDisplayName())) {
            throw new ServiceException.UnprocessableEntityException(String.format("Organization %s not found",
                organization));
        }
    }
}
