package io.jenkins.blueocean.blueocean_bitbucket_pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Vivek Pandey
 */
public abstract class BbSaveContentResponse {
    @JsonProperty("CommitId")
    public abstract String getCommitId();
}
