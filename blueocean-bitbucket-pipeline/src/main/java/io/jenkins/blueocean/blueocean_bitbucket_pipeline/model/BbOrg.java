package io.jenkins.blueocean.blueocean_bitbucket_pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Vivek Pandey
 */
public abstract class BbOrg {
    @JsonProperty("key")
    public abstract String getKey();

    @JsonProperty("name")
    public abstract String getName();

    @JsonProperty("avatar")
    public abstract String getAvatar();

    @JsonProperty("public")
    public abstract boolean isPublicProject();
}
