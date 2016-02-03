package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

/**
 * {@link ProfileService#getOrganization(String)} request
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

