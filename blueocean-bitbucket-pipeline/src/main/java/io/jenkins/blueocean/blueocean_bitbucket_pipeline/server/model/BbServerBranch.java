package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
public class BbServerBranch extends BbBranch {
    private final String latestCommit;
    private final boolean isDefault;
    private final String id;
    private final String displayId;

    @JsonCreator
    public BbServerBranch(@Nonnull @JsonProperty("latestCommit") String latestCommit,
                          @Nonnull @JsonProperty("isDefault") Boolean isDefault, @Nonnull @JsonProperty("id") String id,
                          @Nonnull @JsonProperty("displayId") String displayId) {
        this.latestCommit = latestCommit;
        this.isDefault = isDefault;
        this.id = id;
        this.displayId = displayId;
    }

    @Override
    @JsonProperty("latestCommit")
    public String getLatestCommit() {
        return latestCommit;
    }

    @Override
    @JsonProperty("isDefault")
    public boolean isDefault() {
        return isDefault;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @Override
    @JsonProperty("displayId")
    public String getDisplayId() {
        return displayId;
    }
}