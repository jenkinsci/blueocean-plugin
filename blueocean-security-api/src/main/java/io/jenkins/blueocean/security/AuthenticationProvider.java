package io.jenkins.blueocean.security;

import javax.annotation.Nonnull;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;

public abstract class AuthenticationProvider implements ExtensionPoint {
    /**
     *
     * @return
     */
    public abstract String getLoginUrl();

    /**
     *
     * @return
     */
    public abstract String getPostLogoutUrl();

    /**
     * All registered extension points.
     */
    public static ExtensionList<AuthenticationProvider> all() {
        Jenkins jenkins = Jenkins.getInstance();
        if(jenkins == null) {
            return null;
        } else {
            return jenkins.getExtensionList(AuthenticationProvider.class);
        }
    }

    /**
     * Looks up an authentication provider based on its id.
     *
     * @param id Id that makes AuthenticationProvider.getId()
     * @return The matched authentication provider otherwise null.
     */
    public static AuthenticationProvider getAuthenticationProvider(String id) {
        for(AuthenticationProvider provider: all()){
            if(id.equalsIgnoreCase(provider.getLoginUrl())){
                return provider;
            }
        }
        return null;
    }

    @Nonnull
    public abstract <T extends LoginDetails> LoginDetailsProvider<T> getLoginDetailsProvider();

    public static LoginDetailsProvider<?> getLoginDetailsProvider(Class clzz) {
        for(AuthenticationProvider provider: all()){
            if(provider.getLoginDetailsProvider().getLoginDetalsClass() == clzz) {
                return provider.getLoginDetailsProvider();
            }
        }
        return null;
    }


}
