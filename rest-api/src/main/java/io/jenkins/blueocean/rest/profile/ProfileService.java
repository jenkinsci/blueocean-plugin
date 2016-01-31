package io.jenkins.blueocean.rest.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import hudson.Extension;
import io.jenkins.blueocean.rest.model.Organization;
import io.jenkins.blueocean.rest.model.hal.Link;
import jenkins.model.Jenkins;
import org.apache.tools.ant.ExtensionPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides Organization management APIs
 *
 * @author Vivek Pandey
 **/
public abstract class ProfileService extends ExtensionPoint{
    /**
     * Gives list of Organizations
     *
     * @return list of discovered organization
     */
    public abstract FindOrganizationsResponse findOrganizations();


    public static class FindOrganizationsResponse{
        @JsonProperty("organizations")
        public final List<Organization> organizations = new ArrayList<Organization>();
    }


    @Extension
    public static class StandaloneProfileService extends ProfileService {

        @Override
        public FindOrganizationsResponse findOrganizations() {
            Organization org = new Organization.Builder(Jenkins.getInstance()
                    .getFullName())
                    .version(Jenkins.VERSION)
                    .links(new Organization.OrganizationLinks(new Link(Jenkins.getActiveInstance().getRootUrl()), null, null))
                    .build();
            FindOrganizationsResponse resp = new FindOrganizationsResponse();
            resp.organizations.add(org);
            return resp;
        }

    }

}
