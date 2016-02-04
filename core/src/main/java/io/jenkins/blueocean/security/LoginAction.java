package io.jenkins.blueocean.security;

import hudson.ExtensionList;

/**
 * Created by ivan on 5/02/16.
 */
public class LoginAction {

    public ExtensionList<AuthenticationProvider> getProviders() {
        System.out.println(AuthenticationProvider.all().size()+"");

        return AuthenticationProvider.all();
    }
    public AuthenticationProvider getProvider(String id) {
        System.out.println(AuthenticationProvider.all().size()+"");
        return AuthenticationProvider.getAuthenticationProvider(id);

    }
}

