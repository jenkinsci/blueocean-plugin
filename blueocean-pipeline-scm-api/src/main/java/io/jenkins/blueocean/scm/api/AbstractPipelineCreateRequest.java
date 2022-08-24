package io.jenkins.blueocean.scm.api;

import hudson.model.Item;
import hudson.model.Items;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipelineCreateRequest;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.springframework.security.core.Authentication;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;

/**
 * @author Vivek Pandey
 */
public abstract class AbstractPipelineCreateRequest extends BluePipelineCreateRequest {

    protected final BlueScmConfig scmConfig;

    public AbstractPipelineCreateRequest(String name, BlueScmConfig scmConfig) {
        setName(name);
        this.scmConfig = scmConfig;
    }


    protected @NonNull TopLevelItem createProject(String name, String descriptorName, Class<? extends TopLevelItemDescriptor> descriptorClass, BlueOrganization organization) throws IOException {
        ModifiableTopLevelItemGroup p = getParent(organization);

        final ACL acl = (p instanceof AccessControlled) ? ((AccessControlled) p).getACL() : Jenkins.get().getACL();
        Authentication a = Jenkins.getAuthentication2();
        if(!acl.hasPermission2(a, Item.CREATE)){
            throw new ServiceException.ForbiddenException(
                    String.format("Failed to create pipeline: %s. User %s doesn't have Job create permission", name, a.getName()));
        }
        TopLevelItemDescriptor descriptor = Items.all().findByName(descriptorName);
        if(descriptor == null || !(descriptorClass.isAssignableFrom(descriptor.getClass()))){
            throw new ServiceException.BadRequestException(String.format("Failed to create pipeline: %s, descriptor %s is not found", name, descriptorName));
        }

        if (!descriptor.isApplicableIn(p)) {
            throw new ServiceException.ForbiddenException(
                    String.format("Failed to create pipeline: %s. Pipeline can't be created in Jenkins root folder", name));
        }

        if (!acl.hasCreatePermission2(a, p, descriptor)) {
            throw new ServiceException.ForbiddenException("Missing permission: " + Item.CREATE.group.title+"/"+Item.CREATE.name + " " + Item.CREATE + "/" + descriptor.getDisplayName());
        }
        return p.createProject(descriptor, name, true);
    }

    protected User checkUserIsAuthenticatedAndHasItemCreatePermission(BlueOrganization organization) {
        ModifiableTopLevelItemGroup p = getParent(organization);

        User authenticatedUser = User.current();
        if (authenticatedUser == null) {
            throw new ServiceException.UnauthorizedException("Must be logged in to create a pipeline");
        }
        Authentication authentication = Jenkins.getAuthentication2();
        ACL acl = (p instanceof AccessControlled) ? ((AccessControlled) p).getACL() : Jenkins.get().getACL();
        if(!acl.hasPermission2(authentication, Item.CREATE)){
            throw new ServiceException.ForbiddenException(
                String.format("User %s doesn't have Job create permission", authenticatedUser.getId()));
        }
        return authenticatedUser;
    }

    protected abstract String computeCredentialId(BlueScmConfig scmConfig);

    protected ModifiableTopLevelItemGroup getParent(BlueOrganization organization) {
        ModifiableTopLevelItemGroup m = OrganizationFactory.getItemGroup(organization);
        return m!=null? m:Jenkins.get();
    }
}
