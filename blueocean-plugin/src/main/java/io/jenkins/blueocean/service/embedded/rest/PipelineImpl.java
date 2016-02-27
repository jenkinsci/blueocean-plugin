package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Job;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BlueBranch;
import io.jenkins.blueocean.rest.model.BlueBranchContainer;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.DELETE;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author Kohsuke Kawaguchi
 */
public class PipelineImpl extends BluePipeline {
    /*package*/ final Job job;

    protected PipelineImpl(Job job) {
        this.job = job;
    }

    @Override
    public String getOrganization() {
        return OrganizationImpl.INSTANCE.getName();
    }

    @Override
    public String getName() {
        return job.getName();
    }

    @Override
    public String getDisplayName() {
        return job.getDisplayName();
    }

    @Override
    public BlueBranchContainer getBranches() {
        return new BlueBranchContainer() {
            @Override
            public BlueBranch get(String name) {
                throw new ServiceException.NotFoundException(String.format("No branch with name: %s found.", name));
            }

            @Override
            public Iterator<BlueBranch> iterator() {
                return Collections.<BlueBranch>emptyList().iterator();
            }
        };
    }

    @Override
    public BlueRunContainer getRuns() {
        return new RunContainerImpl(this, job);
    }

    @WebMethod(name="") @DELETE
    public void delete() throws IOException, InterruptedException {
        job.delete();
    }
}
