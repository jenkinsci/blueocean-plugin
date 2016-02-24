package io.jenkins.blueocean.security;

import com.google.inject.Inject;
import io.jenkins.blueocean.api.authentication.AuthenticateRequest;
import io.jenkins.blueocean.api.authentication.AuthenticateResponse;
import io.jenkins.blueocean.api.authentication.AuthenticationService;
import io.jenkins.blueocean.api.profile.CreateUserRequest;
import io.jenkins.blueocean.api.profile.ProfileService;
import io.jenkins.blueocean.api.profile.model.UserDetails;
import io.jenkins.blueocean.commons.ServiceException.NotFoundException;
import io.jenkins.blueocean.commons.ServiceException.UnprocessableEntityException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

/**
 * Generic GET or POST action that converts a HttpServlet Request into a Credentials
 */

public final class AuthenticationAction {

    private final AuthenticationService auth;
    private final ProfileService profileService;
    private final Cookies cookies;

    @Inject
    public AuthenticationAction(AuthenticationService auth, ProfileService profileService, Cookies cookies) {
        this.auth = auth;
        this.profileService = profileService;
        this.cookies = cookies;
    }

    // KK: Ivan, we need to talk, there's a better way to do this
    public final void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException {
        String authType = req.getRestOfPath().substring(1, req.getRestOfPath().length());
        AuthenticationProvider provider = AuthenticationProvider.getForType(authType);
        if (provider == null) {
            throw new UnprocessableEntityException("unknown auth provider authType " + authType);
        }
        Credentials credentials = provider.getCredentials(req);
        AuthenticateRequest authReq = new AuthenticateRequest(authType, credentials);
        AuthenticateResponse authResp = auth.authenticate(Identity.ANONYMOUS, authReq);
        UserDetails userDetails;
        if (authResp.userDetails != null) {
            userDetails = authResp.userDetails;
        } else if (provider.allowSignup()) {
            userDetails = profileService.createUser(Identity.ANONYMOUS,
                CreateUserRequest.fromUserPrototype(authResp.userPrototype, authType, credentials)).userDetails;
        } else {
            throw new NotFoundException("cant find user");
        }
        Identity identity = new Identity(userDetails.id);
        cookies.writeAuthCookieToken(rsp, identity);
        rsp.sendRedirect(req.getContextPath());
    }
}
