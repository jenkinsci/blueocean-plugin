package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.api.profile.model.Organization;
import io.jenkins.blueocean.security.Identity;

import javax.annotation.Nullable;

/**
 * Response for createOrganization(Identity, CreateOrganizationRequest)}
 *
 * @author Vivek Pandey
 */
public class CreateOrganizationResponse {

    @JsonProperty("organization")
    public final Organization organization;

    public CreateOrganizationResponse(@Nullable @JsonProperty("organization")Organization organization) {
        this.organization = organization;
    }
}
