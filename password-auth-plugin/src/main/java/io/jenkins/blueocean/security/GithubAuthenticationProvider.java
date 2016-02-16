package io.jenkins.blueocean.security;

import hudson.Extension;
import io.jenkins.blueocean.commons.ServiceException.UnauthorizedException;

import javax.servlet.http.HttpServletRequest;

@Extension
public class GithubAuthenticationProvider extends AuthenticationProvider<GithubCredentials> {
    @Override
    public GithubCredentials getCredentials(HttpServletRequest req) {
        return new GithubCredentials(req.getParameter("login"), req.getParameter("code"));
    }

    @Override
    public String validate(GithubCredentials loginDetails) {
        if (!isTokenValid(loginDetails.login, loginDetails.accessToken)) {
            throw new UnauthorizedException("bad credentials");
        }
        return loginDetails.login;
    }

    @Override
    public String getType() {
        return "github";
    }

    boolean isTokenValid(String login, String token) {
        return true; // TODO: replace with call to github to validate that the token is good
    }
}
