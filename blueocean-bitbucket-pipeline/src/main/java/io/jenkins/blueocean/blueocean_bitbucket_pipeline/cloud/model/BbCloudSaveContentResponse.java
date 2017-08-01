package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model;

import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbSaveContentResponse;

/**
 * @author Vivek Pandey
 */
public class BbCloudSaveContentResponse extends BbSaveContentResponse{
    private final String commitId;

    public BbCloudSaveContentResponse(String commitId) {
        this.commitId = commitId;
    }

    @Override
    public String getCommitId() {
        return commitId;
    }
}
