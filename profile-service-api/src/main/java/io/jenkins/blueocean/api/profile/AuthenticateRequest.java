package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

import io.jenkins.blueocean.security.LoginDetails;

/**
 * Request for {@link ProfileService#authenticate(AuthenticateRequest)}
 *
 * @author Ivan Meredith
 */
public class AuthenticateRequest {

    @JsonProperty("loginDetails")
    public final LoginDetails loginDetails;

    public AuthenticateRequest(@Nonnull @JsonProperty("loginDetails") LoginDetails loginDetails) {
        this.loginDetails = loginDetails;
    }
}
