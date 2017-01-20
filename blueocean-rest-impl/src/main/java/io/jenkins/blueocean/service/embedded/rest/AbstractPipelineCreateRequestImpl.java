package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.security.ACL;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BluePipelineCreateRequest;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author Vivek Pandey
 */
public abstract class AbstractPipelineCreateRequestImpl extends BluePipelineCreateRequest {

    public @Nonnull TopLevelItem create(ModifiableTopLevelItemGroup parent, String name, String descriptorName, Class<? extends TopLevelItemDescriptor> descriptorClass) throws IOException {
        ACL acl = Jenkins.getInstance().getACL();
        acl.checkPermission(Item.CREATE);
        TopLevelItemDescriptor descriptor = Items.all().findByName(descriptorName);
        if(descriptor == null || !(descriptorClass.isAssignableFrom(descriptorClass))){
            throw new ServiceException.BadRequestExpception(String.format("Failed to create pipeline: %s, descriptor %s is not found", name, descriptorName));
        }
        ItemGroup p = Jenkins.getInstance();
        descriptor.checkApplicableIn(p);

        acl.checkCreatePermission(p, descriptor);
        try {
            return parent.createProject(descriptor, name, true);
        }catch (IllegalArgumentException e){
            throw new ServiceException.BadRequestExpception(new ErrorMessage(400, "Failed to create Git pipeline: "+name)
                    .add(new ErrorMessage.Error("name",
                            ErrorMessage.Error.ErrorCodes.ALREADY_EXISTS.toString(), name+" already exists")));
        }
    }
}
