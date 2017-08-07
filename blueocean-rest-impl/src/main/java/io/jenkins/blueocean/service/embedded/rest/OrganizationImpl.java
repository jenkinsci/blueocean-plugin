package io.jenkins.blueocean.service.embedded.rest;

import hudson.ExtensionList;
import hudson.model.Action;
import hudson.model.ItemGroup;
import hudson.model.User;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.ApiHead;
import io.jenkins.blueocean.rest.OrganizationRoute;
import io.jenkins.blueocean.rest.factory.organization.AbstractOrganization;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BlueUser;
import io.jenkins.blueocean.rest.model.BlueUserContainer;
import io.jenkins.blueocean.rest.model.GenericResource;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.accmod.restrictions.None;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.verb.DELETE;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * {@link BlueOrganization} implementation for the embedded use.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public class OrganizationImpl extends AbstractOrganization{
    private final String name;
    /**
     * Everything in this {@link ItemGroup} is considered to belong to this organization.
     */
    private final ModifiableTopLevelItemGroup group;

    private final UserContainerImpl users = new UserContainerImpl(this, this);

    public OrganizationImpl(String name, ModifiableTopLevelItemGroup group) {
        this.name = name;
        this.group = group;
    }

    /**
     * In embedded mode, there's only one organization
     */
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public ModifiableTopLevelItemGroup getGroup() {
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
        return new UserImpl(this, user, new UserContainerImpl(this, this));
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
            String urlName = action.getUrlName();
            if (urlName != null && urlName.equals(route)) {
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

    private static final Pattern pattern = Pattern.compile("/blue/organizations/([^/]*)/");

    /**
     * Gets the organization from the URL in the form of:
     * 
     * <pre>
     * /blue/organizations/ORG_NAME/...
     * </pre>
     * 
     * Defaulting to the first organization if not found
     * 
     * NOTE: designed for exclusive use in preloaders or places where its not possible to get an organization ref in
     * scope
     * 
     * @return the organization name if found, {code}null{code} if not
     */
    @CheckForNull
    @Restricted(None.class) //Should only be used for preloaders
    public static BlueOrganization getOrganizationFromURL() {
        String organizationName = null;
        StaplerRequest currentRequest = Stapler.getCurrentRequest();
        if (currentRequest != null) {
            String requestURI = currentRequest.getRequestURI();
            if (requestURI != null) {
                Matcher matcher = pattern.matcher(requestURI);
                if (matcher.find()) {
                    organizationName = matcher.group(1);
                }
            }
        }

        return Objects.firstNonNull(OrganizationFactory.getInstance().get(organizationName), Iterables.getFirst(OrganizationFactory.getInstance().list(), null));
    }
}
