package io.jenkins.blueocean.blueocean_bitbucket_pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * BitBucket Branch
 *
 * @author Vivek Pandey
 */
@Restricted(NoExternalUse.class)
public abstract class BbBranch {
    /**
     * @return latest commit id
     */
    @JsonProperty("latestCommit")
    public abstract String getLatestCommit();

    /**
     * @return True if it's default branch, false otherwise
     */
    @JsonProperty("isDefault")
    public abstract boolean isDefault();

    /**
     *
     * @return Branch display identifier
     */
    @JsonProperty("displayId")
    public abstract String getDisplayId();
}
