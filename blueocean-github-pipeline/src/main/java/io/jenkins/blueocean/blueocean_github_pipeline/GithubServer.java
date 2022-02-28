package io.jenkins.blueocean.blueocean_github_pipeline;

import io.jenkins.blueocean.commons.DigestUtils;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpoint;
import org.jenkinsci.plugins.github_branch_source.Endpoint;

public class GithubServer extends ScmServerEndpoint {
    private final Endpoint endpoint;
    private final Link parent;

    GithubServer(Endpoint endpoint, Link parent) {
        this.endpoint = endpoint;
        this.parent = parent;
    }

    @Override
    public String getId() {
        return DigestUtils.sha256(endpoint.getApiUri());
    }

    @Override
    public String getName() {
        return endpoint.getName();
    }

    @Override
    public String getApiUrl() {
        return endpoint.getApiUri();
    }

    @Override
    public Link getLink() {
        return parent.rel(getId());
    }
}
