package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Project;
import io.jenkins.blueocean.rest.sandbox.BOBranchContainer;
import io.jenkins.blueocean.rest.sandbox.BOPipeline;
import io.jenkins.blueocean.rest.sandbox.BORunContainer;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.DELETE;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class PipelineImpl extends BOPipeline {
    /* package */final Project project;

    protected PipelineImpl(Project project) {
        this.project = project;
    }

    @Override
    public String getOrganization() {
        return OrganizationImpl.INSTANCE.getName();
    }

    @Override
    public String getName() {
        return project.getName();
    }

    @Override
    public String getDisplayName() {
        return project.getDisplayName();
    }

    @Override
    public BOBranchContainer getBranches() {
        return new BranchContainerImpl(this);
    }

    @Override
    public BORunContainer getRuns() {
        return new RunContainerImpl(this);
    }

    @WebMethod(name="") @DELETE
    public void delete() throws IOException, InterruptedException {
        project.delete();
    }
}
