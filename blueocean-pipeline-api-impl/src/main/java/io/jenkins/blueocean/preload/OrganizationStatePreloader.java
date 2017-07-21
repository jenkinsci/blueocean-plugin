package io.jenkins.blueocean.preload;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import io.jenkins.blueocean.commons.PageStatePreloader;
import io.jenkins.blueocean.rest.factory.organization.AbstractOrganization;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import net.sf.json.util.JSONBuilder;

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
        String orgName = getOrganizationFromURL();
        BlueOrganization organization = null;
        
        OrganizationFactory orgFactory = OrganizationFactory.getInstance();
        if (orgName != null) {
            organization = orgFactory.get(orgName);
        }

        if (organization == null) {
            Iterator<BlueOrganization> iterator = orgFactory.list().iterator();
            if (iterator.hasNext()) {
                organization = iterator.next();
            }
        }

        if(organization != null) {
            String organizationGroup = "/"; //default is root group
            if (organization instanceof AbstractOrganization) {
                organizationGroup = "/" + ((AbstractOrganization) organization).getGroup().getFullName();
            }

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

    private static final Pattern pattern = Pattern.compile("/blue/organizations/([^/]*)/");

    private String getOrganizationFromURL() {
        StaplerRequest currentRequest = Stapler.getCurrentRequest();
        if (currentRequest == null) {
            return null;
        }

        String requestURI = currentRequest.getRequestURI();

        if (requestURI == null) {
            return null;
        }

        Matcher matcher = pattern.matcher(requestURI);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
