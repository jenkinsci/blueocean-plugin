package io.jenkins.blueocean.service.embedded.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;
import org.kohsuke.stapler.export.Exported;

import hudson.model.Job;
import hudson.model.JobProperty;
import io.jenkins.blueocean.rest.model.BlueBranch;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.rest.model.Resource;
import jenkins.branch.Branch;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.actions.ChangeRequestAction;

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
        SCMHead head = SCMHead.HeadByItem.findHead(job);
        if(head != null) {
            ChangeRequestAction action = head.getAction(ChangeRequestAction.class);
            if(action != null){
                return new PullRequest(action.getId(), action.getURL().toExternalForm(), action.getTitle(), action.getAuthor());
            }
        }
        return null;
    }

    public static class PullRequest extends Resource {
        private static final String PULL_REQUEST_NUMBER = "id";
        private static final String PULL_REQUEST_AUTHOR = "author";
        private static final String PULL_REQUEST_TITLE = "title";
        private static final String PULL_REQUEST_URL = "url";

        private final String id;

        private final String url;

        private final String title;

        private final String author;

        public PullRequest(String id, String url, String title, String author) {
            this.id = id;
            this.url = url;
            this.title = title;
            this.author = author;
        }

        @Exported(name = PULL_REQUEST_NUMBER)
        @JsonProperty(PULL_REQUEST_NUMBER)
        public String getId() {
            return id;
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


        @Exported(name = PULL_REQUEST_AUTHOR)
        @JsonProperty(PULL_REQUEST_AUTHOR)
        public String getAuthor() {
            return author;
        }
    }

}
