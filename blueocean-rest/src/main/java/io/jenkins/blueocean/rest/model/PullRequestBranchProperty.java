package io.jenkins.blueocean.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.kohsuke.stapler.export.Exported;

/**
 * All pipelines that have a pull request concept should use this property.
 *
 * @author Ivan Meredith
 */
public abstract class PullRequestBranchProperty implements BlueBranchProperty{
    public static final String URL = "url";
    public static final String DESCRIPTION = "description";

    @Exported(name = URL)
    @JsonProperty(URL)
    public abstract String getUrl();

    @Exported(name = DESCRIPTION)
    @JsonProperty(DESCRIPTION)
    public abstract String getDescription();
}
