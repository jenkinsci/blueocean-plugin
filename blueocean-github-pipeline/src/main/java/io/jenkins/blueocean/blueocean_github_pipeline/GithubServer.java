package io.jenkins.blueocean.blueocean_github_pipeline;

import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpoint;
import org.jenkinsci.plugins.github_branch_source.Endpoint;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.verb.GET;

public class GithubServer extends ScmServerEndpoint {
    private final Endpoint endpoint;

    GithubServer(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Exported(name = NAME)
    public String getName() {
        return endpoint.getName();
    }

    @Exported(name = API_URL)
    public String getApiUrl() {
        return endpoint.getApiUri();
    }

    @WebMethod(name="") @GET
    @TreeResponse
    public Object getState() {
        return this;
    }
}
