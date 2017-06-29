package io.jenkins.blueocean.blueocean_bitbucket_pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Response from saving content to Bitbucket.
 *
 * @author Vivek Pandey
 */
@Restricted(NoExternalUse.class)
public abstract class BbSaveContentResponse {
    /**
     * @return commit id of the saved content
     */
    @JsonProperty("CommitId")
    public abstract String getCommitId();
}
