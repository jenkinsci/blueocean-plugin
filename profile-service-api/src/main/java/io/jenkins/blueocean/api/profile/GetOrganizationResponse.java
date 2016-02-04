package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.api.profile.model.Organization;
import io.jenkins.blueocean.commons.Identity;

import javax.annotation.Nullable;

/**
 * Response for for {@link ProfileService#getOrganization(Identity, GetOrganizationRequest)}
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

