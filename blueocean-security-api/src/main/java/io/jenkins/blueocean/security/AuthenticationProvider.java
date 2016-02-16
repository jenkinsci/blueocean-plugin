package io.jenkins.blueocean.security;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import hudson.ExtensionList;
import jenkins.model.Jenkins;

import javax.servlet.http.HttpServletRequest;

public abstract class AuthenticationProvider<T extends Credentials> {

    /** Retrive the credentials from the request */
    public abstract T getCredentials(HttpServletRequest req);

    /** Validate the credentials. Only called by AuthenticationService */
    public abstract String validate(T loginDetails);

    /** The type of this provider */
    public abstract String getType();

    /** Look up a provider instance for the given type */
    public static AuthenticationProvider getForType(final String type) {
        Jenkins j = Jenkins.getInstance();
        if(j == null) {
            throw new IllegalStateException("jenkins instance null");
        }
        ExtensionList<AuthenticationProvider> extensionList = j.getExtensionList(AuthenticationProvider.class);
        return Iterators.find(extensionList.iterator(), new Predicate<AuthenticationProvider>() {
            @Override
            public boolean apply(AuthenticationProvider authenticationProvider) {
                return type.equals(authenticationProvider.getType());
            }
        });
    }
}
