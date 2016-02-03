package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.api.profile.model.Organization;

import javax.annotation.Nullable;

/**
 * {@link ProfileService#getOrganization(GetOrganizationRequest)} response
 *
 * @author Vivek Pandey
 */
public final class GetOrganizationResponse{
    @JsonProperty("organization")
    public final Organization organization;

    public GetOrganizationResponse(@Nullable @JsonProperty("organization")Organization organization) {
        this.organization = organization;
    }
}

