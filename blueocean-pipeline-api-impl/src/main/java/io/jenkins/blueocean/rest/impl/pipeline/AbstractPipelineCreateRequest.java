package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.security.ACL;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BluePipelineCreateRequest;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author Vivek Pandey
 */
public abstract class AbstractPipelineCreateRequest extends BluePipelineCreateRequest {

    protected final BlueScmConfig scmConfig;

    @DataBoundConstructor
    public AbstractPipelineCreateRequest(String name, BlueScmConfig scmConfig) {
        setName(name);
        this.scmConfig = scmConfig;
    }

    protected  @Nonnull TopLevelItem createProject(String name, String descriptorName, Class<? extends TopLevelItemDescriptor> descriptorClass) throws IOException{
        ACL acl = Jenkins.getInstance().getACL();
        Authentication a = Jenkins.getAuthentication();
        if(!acl.hasPermission(a, Item.CREATE)){
            throw new ServiceException.ForbiddenException(
                    String.format("Failed to create pipeline: %s. User %s doesn't have Job create permission", name, a.getName()));
        }
        TopLevelItemDescriptor descriptor = Items.all().findByName(descriptorName);
        if(descriptor == null || !(descriptorClass.isAssignableFrom(descriptor.getClass()))){
            throw new ServiceException.BadRequestExpception(String.format("Failed to create pipeline: %s, descriptor %s is not found", name, descriptorName));
        }

        ItemGroup p = Jenkins.getInstance();
        if(!descriptor.isApplicableIn(p)){
            throw new ServiceException.ForbiddenException(
                    String.format("Failed to create pipeline: %s. pipeline can't be created in Jenkins root folder", name));
        }

        if(!acl.hasCreatePermission(a, p, descriptor)){
            throw new ServiceException.ForbiddenException("Missing permission: " +Item.CREATE.group.title+"/"+Item.CREATE.name + Item.CREATE + "/" + descriptor.getDisplayName());
        }
        return Jenkins.getInstance().createProject(descriptor, name, true);
    }
}
