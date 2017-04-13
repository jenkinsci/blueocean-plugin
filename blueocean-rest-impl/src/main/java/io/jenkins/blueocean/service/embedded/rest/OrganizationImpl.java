package io.jenkins.blueocean.service.embedded.rest;

import hudson.ExtensionList;
import hudson.model.Action;
import hudson.model.ItemGroup;
import hudson.model.User;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.JsonBody;
import io.jenkins.blueocean.rest.ApiHead;
import io.jenkins.blueocean.rest.OrganizationRoute;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BlueUser;
import io.jenkins.blueocean.rest.model.BlueUserContainer;
import io.jenkins.blueocean.rest.model.GenericResource;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.verb.DELETE;
import org.kohsuke.stapler.verb.PUT;

import java.io.IOException;

/**
 * {@link BlueOrganization} implementation for the embedded use.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public class OrganizationImpl extends BlueOrganization {
    private final String name;
    /**
     * Everything in this {@link ItemGroup} is considered to belong to this organization.
     */
    private final ItemGroup group;

    private final UserContainerImpl users = new UserContainerImpl(this);

    public OrganizationImpl(String name, ItemGroup group) {
        this.name = name;
        this.group = group;
    }

    /**
     * In embedded mode, there's only one organization
     */
    public String getName() {
        return name;
    }

    public ItemGroup getGroup() {
        return group;
    }

    @Override
    public String getDisplayName() {
        return "Jenkins";
    }

    @Override
    public BluePipelineContainer getPipelines() {
        return new PipelineContainerImpl(group);
    }

    @WebMethod(name="") @DELETE
    public void delete() {
        throw new ServiceException.NotImplementedException("Not implemented yet");
    }

    @WebMethod(name="") @PUT
    public void update(@JsonBody OrganizationImpl given) throws IOException {
        given.validate();
        throw new ServiceException.NotImplementedException("Not implemented yet");
//        getXmlFile().write(given);
    }

    private void validate() {
//        if (name.length()<2)
//            throw new IllegalArgumentException("Invalid name: "+name);
    }

    /**
     * In the embedded case, there's only one organization and everyone belongs there,
     * so we can just return that singleton.
     */
    @Override
    public BlueUserContainer getUsers() {
        return users;
    }

    @Override
    public BlueUser getUser() {
        User user =  User.current();
        if(user == null){
            throw new ServiceException.NotFoundException("No authenticated user found");
        }
        return new UserImpl(user,new UserContainerImpl(this));
    }

    @Override
    public Link getLink() {
        return ApiHead.INSTANCE().getLink().rel("organizations/"+getName());
    }

    /**
     * Give plugins chance to handle this API route.
     *
     * @param route URL path that needs handling. e.g. for requested url /rest/organizations/:id/xyz,  route param value will be 'xyz'
     * @return stapler object that can handle give route. Could be null
     */
    public Object getDynamic(String route){
        //First look for OrganizationActions
        for(OrganizationRoute organizationRoute: ExtensionList.lookup(OrganizationRoute.class)){
            if(organizationRoute.getUrlName() != null && organizationRoute.getUrlName().equals(route)){
                return wrap(organizationRoute);
            }
        }

        // No OrganizationRoute found, now lookup in available actions from Jenkins instance serving root
        for(Action action:Jenkins.getInstance().getActions()) {
            if (action.getUrlName() != null && action.getUrlName().equals(route)) {
                return wrap(action);
            }
        }
        return null;
    }

    private Object wrap(Object action){
        if (isExportedBean(action.getClass())) {
            return action;
        } else {
            return new GenericResource<>(action);
        }
    }

    private boolean isExportedBean(Class clz){
        return clz.getAnnotation(ExportedBean.class) != null;
    }
}
