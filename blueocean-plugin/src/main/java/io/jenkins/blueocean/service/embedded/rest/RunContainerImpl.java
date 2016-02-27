package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Job;
import hudson.util.RunList;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import jenkins.model.Jenkins;

import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class RunContainerImpl extends BlueRunContainer {

    private final Job job;
    private final BluePipeline pipeline;

    public RunContainerImpl(BluePipeline pipeline, Job job) {
        this.job = job;
        this.pipeline = pipeline;
    }


    @Override
    public BlueRun get(String name) {
        List<Job> projects = Jenkins.getActiveInstance().getAllItems(Job.class);
        for (Job p : projects) {
            if (!p.getName().equals(job.getName())) {
                continue;
            }
            RunList<? extends hudson.model.Run> runList = p.getBuilds();

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
            return  AbstractBlueRun.getBlueRun(run);
        }
        throw new ServiceException.NotFoundException(String.format("Run id %s not found for organization %s, pipeline: %s",
            name, pipeline.getOrganization(), job.getName()));
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
