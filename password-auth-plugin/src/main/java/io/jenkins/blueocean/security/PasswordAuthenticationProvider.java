package io.jenkins.blueocean.security;

import io.jenkins.blueocean.commons.ServiceException.UnauthorizedException;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jdumay on 11/02/2016.
 */
public class PasswordAuthenticationProvider extends AuthenticationProvider<PasswordCredentials> {
    @Override
    public PasswordCredentials getCredentials(HttpServletRequest req) {
        return new PasswordCredentials(req.getParameter("username"), req.getParameter("password"));
    }

    @Override
    public String validate(PasswordCredentials loginDetails) {
        String password = hash(loginDetails.password);
        String storedPasswordHash = getPasswordHashForUser(loginDetails.user);
        if (!storedPasswordHash.equals(hash(password))) {
            throw new UnauthorizedException("bad credentials");
        }
        return loginDetails.user;
    }

    @Override
    public String getType() {
        return "basic";
    }

    private String hash(String password) {
        return "users_password_hash";
    }

    private String getPasswordHashForUser(String user) {
        return "users_password_hash";
    }
}
