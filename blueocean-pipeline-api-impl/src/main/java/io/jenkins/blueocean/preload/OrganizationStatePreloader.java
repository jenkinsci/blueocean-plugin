package io.jenkins.blueocean.preload;

import hudson.Extension;
import io.jenkins.blueocean.commons.PageStatePreloader;
import io.jenkins.blueocean.rest.factory.organization.AbstractOrganization;
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
        String organizationGroup="/"; //default is root group
        if(organization instanceof AbstractOrganization){
            organizationGroup = "/"+((AbstractOrganization) organization).getGroup().getFullName();
        }
        if(organization != null) {
            StringWriter writer = new StringWriter();
            new JSONBuilder(writer)
                    .object()
                    .key("name").value(organization.getName())
                    .key("displayName").value(organization.getDisplayName())
                    // org group such as 'folder1/folder2' is going to be returned as '/folder1/folder2'
                    // root 'jenkins' org will be returned as '/'
                    .key("organizationGroup").value(organizationGroup)
                    .endObject();
            return writer.toString();
        }else{
            return "{}";// if will happen only when there is no implementation of BlueOrganization found.
        }
    }
}
