package io.jenkins.blueocean;

import hudson.Extension;
import hudson.ExtensionList;

/**
 * Root of Blue Ocean UI
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class BlueOceanUI {
/* TODO: Ivan(?) needs to push this down into the security-api module
    @Inject
    private ProfileService profiles;
    @Inject
    private LoginAction loginAction;
    @Inject
    private ApplicationConfig appConfig;
    @Inject
    private Cookies cookies;

    public LoginAction getLoginAction() {
        return loginAction;
    }

    public HttpResponse getLogoutAction() {
        cookies.removeAuthCookieToken(Stapler.getCurrentResponse());
        return HttpResponses.redirectTo(appConfig.getApplicationPath());
    }

    public String getCurrentUserFullName() {
        Identity identity = (Identity)Stapler.getCurrentRequest().getUserPrincipal();
        return profiles.getUserDetails(identity, GetUserDetailsRequest.byUserId(identity.user)).userDetails.fullName;
    }

    public String getCurrentUserEmail() {
        Identity identity = (Identity)Stapler.getCurrentRequest().getUserPrincipal();
        return profiles.getUserDetails(identity, GetUserDetailsRequest.byUserId(identity.user)).userDetails.email;
    }
*/

    /**
     * Exposes {@link RootRoutable}s to the URL space.
     */
    public RootRoutable getDynamic(String route) {
        for (RootRoutable r : ExtensionList.lookup(RootRoutable.class)) {
            if (r.getUrlName().equals(route))
                return r;
        }
        return null;
    }
}
