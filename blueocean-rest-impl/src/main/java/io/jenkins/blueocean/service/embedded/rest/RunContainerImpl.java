package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Job;
import hudson.util.RunList;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * @author Vivek Pandey
 */
public class RunContainerImpl extends BlueRunContainer {

    private final Job job;
    private final BluePipeline pipeline;

    public RunContainerImpl(@Nonnull BluePipeline pipeline, @Nonnull Job job) {
        this.job = job;
        this.pipeline = pipeline;
    }


    @Override
    public BlueRun get(String name) {
        RunList<? extends hudson.model.Run> runList = job.getBuilds();

        hudson.model.Run run = null;
        if (name != null) {
            for (hudson.model.Run r : runList) {
                if (r.getId().equals(name)) {
                    run = r;
                    break;
                }
            }
            if (run == null) {
                throw new ServiceException.NotFoundException(
                    String.format("Run %s not found in organization %s and pipeline %s",
                        name, pipeline.getOrganization(), job.getName()));
            }
        } else {
            run = runList.getLastBuild();
        }
        return  AbstractRunImpl.getBlueRun(run);
    }

    @Override
    public Iterator<BlueRun> iterator() {
        return RunSearch.findRuns(job).iterator();
    }

    @Override
    public BluePipeline getPipeline(String name) {
        return pipeline;
    }
}
