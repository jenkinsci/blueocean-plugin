package io.jenkins.blueocean.preload;

import hudson.Extension;
import io.jenkins.blueocean.commons.PageStatePreloader;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import jenkins.model.Jenkins;
import net.sf.json.util.JSONBuilder;

import javax.annotation.Nonnull;
import java.io.StringWriter;

/**
 * @author Vivek Pandey
 */
@Extension
public class OrganizationStatePreloader extends PageStatePreloader {
    @Nonnull
    @Override
    public String getStatePropertyPath() {
        return "organization";
    }

    @Override
    public String getStateJson() {
        BlueOrganization organization = OrganizationFactory.getInstance().getContainingOrg(Jenkins.getInstance());
        if(organization != null) {
            StringWriter writer = new StringWriter();
            new JSONBuilder(writer)
                    .object()
                    .key("name").value(organization.getName())
                    .endObject();
            return writer.toString();
        }else{
            return "{}";// if will happen only when there is no implementation of BlueOrganization found.
        }
    }
}
