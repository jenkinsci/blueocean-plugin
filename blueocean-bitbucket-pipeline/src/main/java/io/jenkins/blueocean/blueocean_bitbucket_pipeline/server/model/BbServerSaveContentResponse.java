package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbSaveContentResponse;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * @author Vivek Pandey
 */
public class BbServerSaveContentResponse extends BbSaveContentResponse {
    private final String id;

    @JsonCreator
    public BbServerSaveContentResponse(@NonNull@JsonProperty("id") String id) {
        this.id = id;
    }

    @Override
    public String getCommitId() {
        return id;
    }
}
