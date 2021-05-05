package io.jenkins.blueocean.blueocean_bitbucket_pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Bitbucket user
 *
 * @author Vivek Pandey
 */
@Restricted(NoExternalUse.class)
public abstract class BbUser {
    /**
     * @return Displayable name
     */
    @JsonProperty("displayName")
    public abstract String getDisplayName();

    /**
     * @return User slug
     */
    @JsonProperty("slug")
    public abstract String getSlug();

    /**
     * @return User email
     */
    @JsonProperty("emailAddress")
    public abstract String getEmailAddress();

    /**
     * @return URL of user avatar
     */
    @JsonProperty("avatar")
    public abstract String  getAvatar();
}
