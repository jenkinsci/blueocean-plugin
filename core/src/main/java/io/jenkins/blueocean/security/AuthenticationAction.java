package io.jenkins.blueocean.security;

import com.google.inject.Inject;
import hudson.Extension;
import io.jenkins.blueocean.api.profile.AuthenticationService;
import io.jenkins.blueocean.api.profile.AuthenticationService.AuthenticateRequest;
import io.jenkins.blueocean.api.profile.model.UserDetails;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

/**
 * Generic GET or POST action that converts a HttpServlet Request into a Credentials
 */
@Extension
public final class AuthenticationAction {

    private final AuthenticationService auth;

    @Inject
    public AuthenticationAction(AuthenticationService auth) {
        this.auth = auth;
    }

    public final void doLogin(StaplerRequest req, StaplerResponse rsp) throws IOException {

        String type = req.getParameter("authType"); // Github, Basic, LDAP
        AuthenticationProvider provider = AuthenticationProvider.getForType(type);

        Credentials credentials = provider.getCredentials(req);
        AuthenticateRequest authReq = new AuthenticateRequest(type, credentials);
        UserDetails userDetails = auth.authenticate(Identity.ANONYMOUS, authReq).userDetails; // Will throw UnauthorisedException if cannot be validated
        Identity identity = new Identity(userDetails.name);
        Cookies cookies = new Cookies();
        cookies.writeAuthCookieToken(rsp, identity);
        rsp.sendRedirect(req.getContextPath());
    }
}
