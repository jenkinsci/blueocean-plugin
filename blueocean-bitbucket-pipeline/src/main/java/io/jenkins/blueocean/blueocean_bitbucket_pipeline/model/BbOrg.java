package io.jenkins.blueocean.blueocean_bitbucket_pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * BitBucket Project or Team.
 *
 * In case of BitBucket server 'Project' is presented as scm org, in case of BitBucket cloud 'Team' is scm org.
 *
 * @author Vivek Pandey
 */
@Restricted(NoExternalUse.class)
public abstract class BbOrg {
    /**
     * @return Bitbucket Project/Team key
     */
    @JsonProperty("key")
    public abstract String getKey();

    /**
     * @return Bitbucket name
     */
    @JsonProperty("name")
    public abstract String getName();

    /**
     * @return URL of Bitbucket project/team avatar
     */
    @JsonProperty("avatar")
    public abstract String getAvatar();

    /**
     * @return true if it's a public project
     */
    @JsonProperty("public")
    public abstract boolean isPublicProject();
}
