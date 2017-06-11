package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbUser;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
public class BbServerUser extends BbUser {
    private final String name;
    private final String displayName;
    private final String slug;
    private final String emailAddress;

    @JsonCreator
    public BbServerUser(@Nonnull @JsonProperty("name") String name, @Nonnull @JsonProperty("displayName") String displayName,
                        @Nonnull @JsonProperty("slug") String slug, @Nonnull @JsonProperty("emailAddress") String emailAddress) {
        this.name = name;
        this.displayName = displayName;
        this.slug = slug;
        this.emailAddress = emailAddress;
    }

    @Override
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @Override
    @JsonProperty("displayName")
    public String getDisplayName() {
        return displayName;
    }

    @Override
    @JsonProperty("slug")
    public String getSlug() {
        return slug;
    }

    @Override
    @JsonProperty("emailAddress")
    public String getEmailAddress() {
        return emailAddress;
    }
}
