package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

import io.jenkins.blueocean.security.Identity;

/**
 * Response for {@link ProfileService#authenticate(AuthenticateRequest)}
 *
 * @author Ivan Meredith
 */
public class AuthenticateResponse {
    @JsonProperty("identity")
    public final Identity identity;

    public AuthenticateResponse(@Nonnull @JsonProperty("identity") Identity identity) {
        this.identity = identity;
    }
}
