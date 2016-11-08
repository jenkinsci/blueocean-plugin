package io.jenkins.blueocean.rest.impl.pipeline;

import com.cloudbees.plugins.credentials.CredentialsStoreAction;
import io.jenkins.blueocean.Routable;
import io.jenkins.blueocean.rest.ApiRoutable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.Container;

import java.util.Iterator;

/**
 * @author Vivek Pandey
 */
public class CredentialApiImpl extends Container<CredentialsStoreAction> implements ApiRoutable {
    private final BlueOrganization organization;
    private final Link self;

    public CredentialApiImpl(BlueOrganization organization) {
        this.organization = organization;
        this.self = organization.getLink().rel("credentials");
    }

    @Override
    public String getUrlName() {
        return "credentials";
    }

    @Override
    public boolean isChildOf(Routable ancestor) {
        return ancestor instanceof BlueOrganization;
    }

    @Override
    public Link getLink() {
        return self;
    }


    @Override
    public CredentialsStoreAction get(String name) {
        return null;
    }

    @Override
    public Iterator<CredentialsStoreAction> iterator() {
        return null;
    }
}