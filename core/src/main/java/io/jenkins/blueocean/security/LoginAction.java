package io.jenkins.blueocean.security;

import hudson.ExtensionList;
import jenkins.model.Jenkins;

/**
 * Created by ivan on 5/02/16.
 */
public class LoginAction {

    public ExtensionList<AuthenticationProvider> getProviders() {
        Jenkins j = Jenkins.getInstance();
        if(j == null) {
            throw new IllegalStateException("jenkins instance null");
        }
        return j.getExtensionList(AuthenticationProvider.class);
    }
    public AuthenticationProvider getProvider(String loginUrl) {
        for(AuthenticationProvider provider: getProviders()){
            if(loginUrl.equals(provider.getLoginUrl())){
                return provider;
            }
        }
        return null;
    }
}

