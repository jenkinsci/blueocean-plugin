package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * @author Vivek Pandey
 */
public class BbServerBranch extends BbBranch {
    private final String latestCommit;
    private final boolean isDefault;
    private final String id;
    private final String displayId;

    @JsonCreator
    public BbServerBranch(@NonNull @JsonProperty("latestCommit") String latestCommit,
                          @NonNull @JsonProperty("isDefault") Boolean isDefault, @NonNull @JsonProperty("id") String id,
                          @NonNull @JsonProperty("displayId") String displayId) {
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