package io.jenkins.blueocean.security;

import hudson.Extension;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.Nonnull;
import java.io.IOException;

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
        Identity identity = getLoginDetailsProvider().authenticate(new PasswordCredentials(req.getParameter("username"), req.getParameter("password")));
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
    public LoginDetailsProvider<PasswordCredentials> getLoginDetailsProvider() {
        return new LoginDetailsProvider<PasswordCredentials>() {
            @Override
            public Class<PasswordCredentials> getLoginDetalsClass() {
                return PasswordCredentials.class;
            }

            @Override
            public Identity authenticate(PasswordCredentials loginDetails) {
                if(loginDetails.user.equals("ivan")) {
                    return new Identity(loginDetails.user);
                } else {
                    return null;
                }
            }
        };
    }
}
