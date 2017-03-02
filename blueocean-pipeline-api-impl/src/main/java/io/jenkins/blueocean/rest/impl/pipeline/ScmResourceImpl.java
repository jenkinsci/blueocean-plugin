package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.BuildableItem;
import hudson.model.Item;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineScm;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.SCMFile;
import jenkins.scm.api.SCMFileSystem;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

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
    }

    @Override
    public Object getContent(@QueryParameter(value = "path", fixEmpty = true) String path, @QueryParameter(value = "type", fixEmpty = true) String type) {
        if(branchJob == null){
            throw new ServiceException.NotFoundException(String.format("pipeline %s is not a branch", item.getFullName()));
        }
        if(type != null && !type.equals("file")){
            throw new ServiceException.BadRequestExpception(String.format("type %s not supported. Only 'file' type supported.", type));
        }
        if(path == null){
            throw new ServiceException.BadRequestExpception("path is required query parameter");
        }

        if(!(item instanceof MultiBranchProject)){
            throw new ServiceException.BadRequestExpception(String.format("%s is not a MultiBranchProject", item.getFullName()));
        }
        MultiBranchProject mbp = (MultiBranchProject) item;
        try {
            if(mbp.getSCMSources().size() < 0){
                return null;
            }

            SCMSource scmSource = (SCMSource) mbp.getSCMSources().get(0);
            SCMHead head = SCMHead.HeadByItem.findHead(branchJob);
            if(head == null){
                throw new ServiceException.BadRequestExpception(String.format("Branch pipeline %s doesn't have scm source setup", branchJob.getFullName()));
            }
            SCMFileSystem scmFileSystem = SCMFileSystem.of(scmSource, head);
            if(scmFileSystem == null){
                throw new ServiceException.NotFoundException("No scm found for branch: "+branchJob);
            }
            final SCMFile file = scmFileSystem.child(path);
            if(file == null || file.getType() == SCMFile.Type.NONEXISTENT){
                throw new ServiceException.NotFoundException(String.format("scm path %s not found", path));
            }
            if(file.isFile()){
                ScmContentProvider scmContentProvider = ScmContentProvider.resolve(mbp);
                if(scmContentProvider != null){
                    return scmContentProvider.getContent(scmSource, file);
                }
            }else{
                throw new ServiceException.BadRequestExpception(String.format("Requested path: %s is not a file", path));
            }
        } catch (IOException | InterruptedException e) {
            throw new ServiceException.UnexpectedErrorException("Failed to get content: "+e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Object saveContent(StaplerRequest staplerRequest) {
        ScmContentProvider scmContentProvider = ScmContentProvider.resolve(item);

        if(scmContentProvider != null){
            return scmContentProvider.saveContent(staplerRequest, item);
        }
        throw new ServiceException.BadRequestExpception("No scm content provider found for pipeline: " + item.getFullName());
    }

    @Override
    public Link getLink() {
        return self;
    }
}
