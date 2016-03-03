package io.jenkins.blueocean.service.embedded.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.kohsuke.stapler.export.Exported;

import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.PullRequestBranchProperty;

/**
 * @author Ivan Meredith
 */
public class GithubPRBranchProperty extends PullRequestBranchProperty{
    public static final String PULL_REQUEST_NUMBER = "pullRequestNumber";

    @Exported(name = PULL_REQUEST_NUMBER)
    @JsonProperty(PULL_REQUEST_NUMBER)
    public final int pullRequestNumber;

    public GithubPRBranchProperty(int pullRequestNumber) {
        this.pullRequestNumber = pullRequestNumber;
    }


    @Override
    public String getUrl() {
        throw new ServiceException.NotImplementedException("PR Url is not implemented yet");
    }

    @Override
    public String getDescription() {
        throw new ServiceException.NotImplementedException("PR Url is not implemented yet");
    }
}
