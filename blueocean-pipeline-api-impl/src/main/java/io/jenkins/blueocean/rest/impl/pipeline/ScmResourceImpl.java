package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.ACL;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineScm;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.springframework.security.core.Authentication;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * @author Vivek Pandey
 */
public class ScmResourceImpl extends BluePipelineScm {
    private final Item item;
    private final BuildableItem branchJob;
    private final Link self;

    public ScmResourceImpl(Item item, Reachable parent) {
        this(item, null, parent);
    }

    public ScmResourceImpl(Item item, BuildableItem branchJob, Reachable parent) {
        this.item = item;
        this.branchJob = branchJob;
        this.self = parent.getLink().rel("scm");
        checkPermission();
    }

    @Override
    public Object getContent(StaplerRequest request) {
        ScmContentProvider scmContentProvider = ScmContentProvider.resolve(item);

        if(scmContentProvider != null){
            return scmContentProvider.getContent(request, item);
        }
        return null;
    }

    @Override
    public Object saveContent(StaplerRequest staplerRequest) {
        ScmContentProvider scmContentProvider = ScmContentProvider.resolve(item);

        if(scmContentProvider != null){
            return scmContentProvider.saveContent(staplerRequest, item);
        }
        throw new ServiceException.BadRequestException("No scm content provider found for pipeline: " + item.getFullName());
    }

    @Override
    public Link getLink() {
        return self;
    }

    private @NonNull User checkPermission(){
        ACL acl;
        if(item.getParent() != null && item.getParent() instanceof OrganizationFolder){
            acl = ((OrganizationFolder) item.getParent()).getACL();
        }else{
            acl = item.getACL();
        }
        Authentication a = Jenkins.getAuthentication2();
        User user = User.get2(a);
        if(user == null){
            throw new ServiceException.UnauthorizedException("No logged in user found");
        }
        if(!acl.hasPermission2(a, Item.CONFIGURE)){
            throw new ServiceException.ForbiddenException(
                    String.format("User %s must have Job configure permission to access content", a.getName()));
        }

        return user;
    }


}
