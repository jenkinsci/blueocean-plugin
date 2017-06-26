package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbUser;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
public class BbCloudUser extends BbUser {
    private final String userName;

    private final String displayName;

    @JsonCreator
    public BbCloudUser(@Nonnull @JsonProperty("username") String userName, @Nonnull @JsonProperty("display_name") String displayName) {
        this.userName = userName;
        this.displayName = displayName;
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getSlug() {
        return userName;
    }

    @Override
    public String getEmailAddress() {
        return null;
    }
}
