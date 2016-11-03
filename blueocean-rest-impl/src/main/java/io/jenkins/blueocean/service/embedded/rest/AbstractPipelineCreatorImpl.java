package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.security.ACL;
import io.jenkins.blueocean.rest.model.BluePipelineCreator;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;

import java.io.IOException;

/**
 * @author Vivek Pandey
 */
public abstract class AbstractPipelineCreatorImpl extends BluePipelineCreator {

    public TopLevelItem create(ModifiableTopLevelItemGroup parent, String name, String descriptorName, Class<? extends TopLevelItemDescriptor> descriptorClass) throws IOException {
        ACL acl = Jenkins.getInstance().getACL();
        acl.checkPermission(Item.CREATE);
        TopLevelItemDescriptor descriptor = Items.all().findByName(descriptorName);
        if(descriptor == null || !(descriptorClass.isAssignableFrom(descriptorClass))){
            return null;
        }
        ItemGroup p = Jenkins.getInstance();
        descriptor.checkApplicableIn(p);

        acl.checkCreatePermission(p, descriptor);

        return parent.createProject(descriptor, name, true);
    }
}
