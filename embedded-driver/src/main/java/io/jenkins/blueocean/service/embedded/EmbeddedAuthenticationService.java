package io.jenkins.blueocean.service.embedded;

import com.google.inject.Inject;
import io.jenkins.blueocean.api.profile.AuthenticationService;
import io.jenkins.blueocean.api.profile.GetUserDetailsRequest;
import io.jenkins.blueocean.api.profile.ProfileService;
import io.jenkins.blueocean.api.profile.model.UserDetails;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.security.AuthenticationProvider;
import io.jenkins.blueocean.security.Identity;

/**
 * TODO: move to its own module
 * Here so I dont have to invent new maven modules
 */
public class EmbeddedAuthenticationService implements AuthenticationService {

    private final ProfileService profiles;

    @Inject
    public EmbeddedAuthenticationService(ProfileService profiles) {
        this.profiles = profiles;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuthenticateResponse authenticate(Identity identity, AuthenticateRequest req) {

        AuthenticationProvider provider = AuthenticationProvider.getForType(req.type);
        if (provider == null) {
            throw new ServiceException.UnprocessableEntityException(req.type + " is unavailable");
        }
        String username = provider.validate(req.credentials);
        UserDetails userDetails = profiles.getUserDetails(Identity.ROOT, new GetUserDetailsRequest(username)).userDetails;
        return new AuthenticateResponse(userDetails);
    }
}
