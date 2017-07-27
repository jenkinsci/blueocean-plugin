package io.jenkins.blueocean.scm.api;

import hudson.model.Item;
import hudson.model.Items;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipelineCreateRequest;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.acegisecurity.Authentication;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Vivek Pandey
 */
public abstract class AbstractPipelineCreateRequest extends BluePipelineCreateRequest {

    protected final BlueScmConfig scmConfig;

    public AbstractPipelineCreateRequest(String name, BlueScmConfig scmConfig) {
        setName(name);
        Collection<BlueOrganization> organizations = OrganizationFactory.getInstance().list();
        if(organizations.isEmpty()){
            throw new ServiceException.BadRequestException(new ErrorMessage(400,
                    "Pipeline creation failed. Failed to find organization"));
        }else {
            setOrganization(organizations.iterator().next().getName());
        }
        this.scmConfig = scmConfig;
    }


    protected  @Nonnull TopLevelItem createProject(String name, String descriptorName, Class<? extends TopLevelItemDescriptor> descriptorClass) throws IOException{
        final ModifiableTopLevelItemGroup itemGroup = getParent();
        TopLevelItemDescriptor descriptor = Items.all().findByName(descriptorName);
        if(descriptor == null || !(descriptorClass.isAssignableFrom(descriptor.getClass()))){
            throw new ServiceException.BadRequestException(String.format("Failed to create pipeline: %s, descriptor %s is not found", name, descriptorName));
        }

        if(!descriptor.isApplicableIn(itemGroup)){
            throw new ServiceException.ForbiddenException(
                    String.format("Failed to create pipeline: %s. Pipeline can't be created in Jenkins root folder", name));
        }

        final ACL acl = (itemGroup instanceof AccessControlled) ? ((AccessControlled) itemGroup).getACL() : Jenkins.getInstance().getACL();
        Authentication authentication = Jenkins.getAuthentication();
        if(!acl.hasCreatePermission(authentication, itemGroup, descriptor)){
            throw new ServiceException.ForbiddenException("Missing permission: " + Item.CREATE.group.title+"/"+Item.CREATE.name + " " + Item.CREATE + "/" + descriptor.getDisplayName());
        }
        return itemGroup.createProject(descriptor, name, true);
    }

    protected ModifiableTopLevelItemGroup getParent() {
        String organization = getOrganization();
        ModifiableTopLevelItemGroup parent =  OrganizationFactory.getItemGroup(getOrganization());
        if(parent == null){
            throw new ServiceException.BadRequestException("Invalid Jenkins organization. " + organization);
        }

        return parent;
    }

    protected User checkUserIsAuthenticatedAndHasItemCreatePermission() {
        User authenticatedUser = User.current();
        if (authenticatedUser == null) {
            throw new ServiceException.UnauthorizedException("Must be logged in to create a pipeline");
        }
        Authentication authentication = Jenkins.getAuthentication();
        final ModifiableTopLevelItemGroup p = getParent();
        final ACL acl = (p instanceof AccessControlled) ? ((AccessControlled) p).getACL() : Jenkins.getInstance().getACL();
        if(!acl.hasPermission(authentication, Item.CREATE)){
            throw new ServiceException.ForbiddenException(
                String.format("User %s doesn't have Job create permission", authenticatedUser.getId()));
        }
        return authenticatedUser;
    }
}
