package io.jenkins.blueocean.api.profile;

import io.jenkins.blueocean.api.profile.model.UserDetails;
import io.jenkins.blueocean.security.Credentials;
import io.jenkins.blueocean.security.Identity;

/**
 * THIS IS ONLY IN PROFILE-SERVICE-API so I dont have to create maven modules
 * TODO: move this
 */
public interface AuthenticationService {

    class AuthenticateRequest {
        public final String type;
        public final Credentials credentials;

        public AuthenticateRequest(String type, Credentials credentials) {
            this.type = type;
            this.credentials = credentials;
        }
    }

    class AuthenticateResponse {
        public final UserDetails userDetails;

        public AuthenticateResponse(UserDetails userDetails) {
            this.userDetails = userDetails;
        }
    }

    AuthenticateResponse authenticate(Identity identity, AuthenticateRequest authReq);
}
