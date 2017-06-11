package io.jenkins.blueocean.blueocean_bitbucket_pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Vivek Pandey
 */
public abstract class BbBranch {
    @JsonProperty("latestCommit")
    public abstract String getLatestCommit();

    @JsonProperty("isDefault")
    public abstract boolean isDefault();

    @JsonProperty("id")
    public abstract String getId();

    @JsonProperty("displayId")
    public abstract String getDisplayId();
}
