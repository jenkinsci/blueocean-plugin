package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.jgit.annotations.NonNull;

import javax.annotation.Nonnull;

/**
 * Bitbucket Cloud Connect Lifecycle payload as described at:
 * <p>
 * https://developer.atlassian.com/bitbucket/descriptor/lifecycle.html
 * </p>
 *
 * @author Vivek Pandey
 */
public class BbCloudConnectLifecyclePayload {
    private final String sharedSecret;
    private final BbCloudTeam team;
    private final String clientKey;

    @JsonCreator
    public BbCloudConnectLifecyclePayload(@NonNull @JsonProperty("user") BbCloudTeam team,
                                          @NonNull @JsonProperty("sharedSecret") String sharedSecret,
                                          @Nonnull @JsonProperty("clientKey") String clientKey) {
        this.sharedSecret = sharedSecret;
        this.team = team;
        this.clientKey = clientKey;
    }

    @JsonIgnore //do not write out as Json
    public String getSharedSecret() {
        return sharedSecret;
    }

    @JsonIgnore //do not write out as Json
    public String getClientKey() {
        return clientKey;
    }

    @JsonProperty("organization")
    public BbCloudTeam getOrganization() {
        return team;
    }
}
