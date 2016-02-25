package io.jenkins.blueocean.service.embedded.rest;

import hudson.tasks.Mailer;
import io.jenkins.blueocean.rest.sandbox.BOUser;
import io.jenkins.blueocean.security.Credentials;
import io.jenkins.blueocean.service.embedded.properties.CredentialsProperty;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link BOUser} implementation backed by in-memory {@link hudson.model.User}
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public class UserImpl extends BOUser {
    private final hudson.model.User user;

    public UserImpl(hudson.model.User user) {
        this.user = user;
    }

    @Override
    public String getId() {
        return user.getId();
    }

    @Override
    public String getFullName() {
        return user.getFullName();
    }

    @Override
    public String getEmail() {
        if (!user.hasPermission(Jenkins.ADMINISTER)) return null;

        Mailer.UserProperty p = user.getProperty(Mailer.UserProperty.class);
        return p != null ? p.getAddress() : null;
    }

    @Override
    public List<Credentials> getCredentials() {
        if (!user.hasPermission(Jenkins.ADMINISTER)) return null;

        CredentialsProperty p = user.getProperty(CredentialsProperty.class);
        if (p==null)        return Collections.emptyList();
        return new ArrayList<>(p.credentials);
    }
}
