package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.security.Identity;

import javax.annotation.Nonnull;

/**
 * Request for {@link ProfileService#getOrganization(Identity, GetOrganizationRequest)}
 *
 * @author Vivek Pandey
 */
public final class GetOrganizationRequest{
    @JsonProperty("name")
    public final String name;

    public GetOrganizationRequest(@Nonnull @JsonProperty("name") String name) {
        this.name = name;
    }
}

