package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbSaveContentResponse;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
public class BbServerSaveContentResponse extends BbSaveContentResponse {
    private final String id;

    @JsonCreator
    public BbServerSaveContentResponse(@Nonnull@JsonProperty("id") String id) {
        this.id = id;
    }

    @Override
    @JsonProperty("id")
    public String getId() {
        return id;
    }
}
