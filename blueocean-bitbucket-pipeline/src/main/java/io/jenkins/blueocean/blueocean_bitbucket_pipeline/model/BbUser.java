package io.jenkins.blueocean.blueocean_bitbucket_pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Vivek Pandey
 */
public abstract class BbUser {
    @JsonProperty("name")
    public abstract String getName();

    @JsonProperty("displayName")
    public abstract String getDisplayName();

    @JsonProperty("slug")
    public abstract String getSlug();

    @JsonProperty("emailAddress")
    public abstract String getEmailAddress();

    @JsonProperty("avatar")
    public abstract String  getAvatar();
}
