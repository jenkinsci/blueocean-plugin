package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.OmniSearch;
import io.jenkins.blueocean.rest.Query;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueUser;
import io.jenkins.blueocean.rest.pageable.Pageable;
import io.jenkins.blueocean.rest.pageable.Pageables;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
@Extension
public class UserSearch extends OmniSearch<BlueUser> {
    @Override
    public String getType() {
        return "user";
    }

    @Override
    public Pageable<BlueUser> search(Query q) {
        List<BlueUser> users = new ArrayList<>();
        BlueOrganization organization = getOrganization(q);
        
        if(organization == null) {
            organization = OrganizationFactory.getInstance().list().iterator().next();
        }
        
        for(hudson.model.User u:hudson.model.User.getAll()){
            users.add(new UserImpl(organization, u));
        }
        return Pageables.wrap(users);
    }

    /**
     * Obtains the organization from the query. It will return an error if the parameter is absent or the organization
     * could not be found.
     * 
     * @param q the query parameter
     * @return the organization if found
     */
    public BlueOrganization getOrganization(Query q) {
        String org = q.param("organization");
        if (org == null) {
            return null;
        }

        BlueOrganization organization = OrganizationFactory.getInstance().get(org); //This could return null and is ok for the UserImpl which will use Jenkins base.
        return organization;
    }
}
