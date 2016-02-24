package io.jenkins.blueocean.service.embedded;

import io.jenkins.blueocean.commons.ServiceException;
import jenkins.model.Jenkins;

/**
 * Abstract class, all embedded API implementations must extend this.
 *
 * @author Vivek Pandey
 */
public abstract class AbstractEmbeddedService {

    protected Jenkins getJenkins(){
        return Jenkins.getActiveInstance();
    };

    protected void validateOrganization(String organization){
        if (!organization.equals(Jenkins.getActiveInstance().getDisplayName().toLowerCase())) {
            throw new ServiceException.UnprocessableEntityException(String.format("Organization %s not found",
                organization));
        }
    }
}
