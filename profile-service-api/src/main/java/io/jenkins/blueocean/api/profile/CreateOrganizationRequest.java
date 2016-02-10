package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.security.Identity;

import javax.annotation.Nonnull;

/**
 * Request for {@link ProfileService#createOrganization(Identity, CreateOrganizationRequest)}
 *
 * @author Vivek Pandey
 */
public class CreateOrganizationRequest {
   /** organization name */
    @JsonProperty("name")
    public final String name;

    public CreateOrganizationRequest(@Nonnull @JsonProperty("name") String name) {
        this.name = name;
    }

}
