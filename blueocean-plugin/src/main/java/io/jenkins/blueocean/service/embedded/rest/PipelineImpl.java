package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Project;
import io.jenkins.blueocean.rest.model.BlueBranchContainer;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.DELETE;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class PipelineImpl extends BluePipeline {
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
    public BlueBranchContainer getBranches() {
        return new BranchContainerImpl(this);
    }

    @Override
    public BlueRunContainer getRuns() {
        return new RunContainerImpl(this);
    }

    @WebMethod(name="") @DELETE
    public void delete() throws IOException, InterruptedException {
        project.delete();
    }
}
