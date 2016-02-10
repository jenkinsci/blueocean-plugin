package io.jenkins.blueocean.security;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

import javax.annotation.Nonnull;

import hudson.Extension;

/**
 * Created by ivan on 5/02/16.
 */
@Extension
public class PasswordAuthentication implements AuthenticationProvider {

    @Override
    public String getLoginUrl() {
        return "default";
    }

    public void doLogin(StaplerRequest req, StaplerResponse rsp) throws IOException {
        Identity identity = getLoginDetailsProvider().authenticate(new PasswordLoginDetails(req.getParameter("username"), req.getParameter("password")));
        if(identity != null) {
            Cookies cookies = new Cookies();
            cookies.writeAuthCookieToken(rsp, identity);
            rsp.sendRedirect(req.getContextPath());
        } else {
            rsp.setStatus(401);
        }

    }

    @Override
    @Nonnull
    public LoginDetailsProvider<PasswordLoginDetails> getLoginDetailsProvider() {
        return new LoginDetailsProvider<PasswordLoginDetails>() {
            @Override
            public Class<PasswordLoginDetails> getLoginDetalsClass() {
                return PasswordLoginDetails.class;
            }

            @Override
            public Identity authenticate(PasswordLoginDetails loginDetails) {
                if(loginDetails.user.equals("ivan")) {
                    return new Identity(loginDetails.user);
                } else {
                    return null;
                }
            }
        };
    }
}
