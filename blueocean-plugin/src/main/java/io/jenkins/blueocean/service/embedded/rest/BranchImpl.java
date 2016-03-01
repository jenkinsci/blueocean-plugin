package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Job;
import io.jenkins.blueocean.rest.model.BlueBranch;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRunContainer;

/**
 * @author Vivek Pandey
 */
public class BranchImpl extends BlueBranch {

    private final String branch;

    private final Job job;
    private final BluePipeline pipeline;

    public BranchImpl(BluePipeline pipeline, Job job) {
        this.pipeline = pipeline;
        this.job = job;
        this.branch = job.getName();
    }

    @Override
    public String getName() {
        return branch;
    }

    @Override
    public int getWeatherScore() {
        return job.getBuildHealth().getScore();
    }

    @Override
    public BlueRunContainer getRuns() {
        return new RunContainerImpl(pipeline, job);
    }
}
