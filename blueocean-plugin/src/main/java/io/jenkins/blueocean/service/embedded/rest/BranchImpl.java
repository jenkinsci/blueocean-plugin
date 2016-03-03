package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableList;

import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;

import java.util.Collection;

import hudson.model.Job;
import hudson.model.JobProperty;
import io.jenkins.blueocean.rest.model.BlueBranch;
import io.jenkins.blueocean.rest.model.BlueBranchProperty;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.rest.model.PullRequestBranchProperty;
import jenkins.branch.Branch;

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

    @Override
    public Collection<BlueBranchProperty> getProperties() {
        PullRequestBranchProperty prProperty = getPRProperty();
        return ImmutableList.<BlueBranchProperty>of(prProperty);
    }

    private PullRequestBranchProperty getPRProperty() {
        JobProperty property = job.getProperty(BranchJobProperty.class);
        if (property != null && property instanceof BranchJobProperty) {
            Branch branch = ((BranchJobProperty) property).getBranch();
            if(branch != null && branch.getHead() != null && branch.getHead() instanceof PullRequestSCMHead) {
                return new GithubPRBranchProperty(((PullRequestSCMHead) branch.getHead()).getNumber());
            }
        }

        return null;
    }
}
