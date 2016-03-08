package io.jenkins.blueocean.service.embedded.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;
import org.kohsuke.stapler.export.Exported;

import hudson.model.Job;
import hudson.model.JobProperty;
import io.jenkins.blueocean.rest.model.BlueBranch;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import jenkins.branch.Branch;

/**
 * @author Vivek Pandey
 */
public class BranchImpl extends BlueBranch {

    private static final String PULL_REQUEST = "pullRequest";
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
    public BlueRun getLatestRun() {
        return AbstractRunImpl.getBlueRun(job.getLastBuild());
    }

    @Override
    public BlueRunContainer getRuns() {
        return new RunContainerImpl(pipeline, job);
    }

    @Exported(name = PULL_REQUEST, inline = true)
    @JsonProperty(PULL_REQUEST)
    public PullRequest getPullRequest() {
        JobProperty property = job.getProperty(BranchJobProperty.class);
        if (property != null && property instanceof BranchJobProperty) {
            Branch branch = ((BranchJobProperty) property).getBranch();
            if(branch != null && branch.getHead() != null && branch.getHead() instanceof PullRequestSCMHead) {
                return new PullRequest(((PullRequestSCMHead) branch.getHead()).getNumber());
            }
        }

        return null;
    }

    static class PullRequest {
        private static final String PULL_REQUEST_NUMBER = "number";
        private static final String PULL_REQUEST_USER = "user";
        private static final String PULL_REQUEST_TITLE = "title";
        private static final String PULL_REQUEST_URL = "url";

        private final int number;

        private final String url;

        private final String title;

        private final String user;

        public PullRequest(int number) {
            this.number = number;
            url = title = user = "Not supported yet";
        }

        @Exported(name = PULL_REQUEST_NUMBER)
        @JsonProperty(PULL_REQUEST_NUMBER)
        public int getNumber() {
            return number;
        }


        @Exported(name = PULL_REQUEST_URL)
        @JsonProperty(PULL_REQUEST_URL)
        public String getUrl() {
            return url;
        }


        @Exported(name = PULL_REQUEST_TITLE)
        @JsonProperty(PULL_REQUEST_TITLE)
        public String getTitle() {
            return title;
        }


        @Exported(name = PULL_REQUEST_USER)
        @JsonProperty(PULL_REQUEST_USER)
        public String getUser() {
            return user;
        }
    }

}
