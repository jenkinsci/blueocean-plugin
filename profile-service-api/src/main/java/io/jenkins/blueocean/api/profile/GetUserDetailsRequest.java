package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.security.Credentials;
import io.jenkins.blueocean.security.Identity;

import javax.annotation.Nullable;

/**
 * Request for {@link ProfileService#getUserDetails(Identity, GetUserDetailsRequest)}
 *
 * @author Vivek Pandey
 */
public final class GetUserDetailsRequest{
    @Nullable
    @JsonProperty("byUserId")
    public final String byUserId;

    @Nullable
    @JsonProperty("byCredentials")
    public final Credentials byCredentials;

    public GetUserDetailsRequest(@Nullable @JsonProperty("byUserId") String byUserId,
                                 @Nullable @JsonProperty("byCredentials") Credentials byCredentials) {
        this.byUserId = byUserId;
        this.byCredentials = byCredentials;
    }

    public static GetUserDetailsRequest byCredentials(Credentials credentials) {
        return new GetUserDetailsRequest(null, credentials);
    }

    public static GetUserDetailsRequest byUserId(String userId) {
        return new GetUserDetailsRequest(userId, null);
    }
}

