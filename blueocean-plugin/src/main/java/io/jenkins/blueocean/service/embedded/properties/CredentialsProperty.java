package io.jenkins.blueocean.service.embedded.properties;

import hudson.model.UserProperty;
import io.jenkins.blueocean.security.Credentials;

import java.util.Set;

public class CredentialsProperty extends UserProperty {
    public final Set<Credentials> credentials;

    public CredentialsProperty(Set<Credentials> credentials) {
        this.credentials = credentials;
    }
}
