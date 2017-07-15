package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.Resource;
import org.jenkinsci.plugins.github_branch_source.Endpoint;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class GithubServer extends Resource {

    public static final String ID = "ID";
    public static final String NAME = "name";
    public static final String API_URL = "apiUrl";

    private final Endpoint endpoint;
    private final Link parent;

    GithubServer(Endpoint endpoint, Link parent) {
        this.endpoint = endpoint;
        this.parent = parent;
    }

    @Exported(name = ID)
    public String getId() {
        return Hashing.sha256().hashString(endpoint.getApiUri(), Charsets.UTF_8).toString();
    }

    @Exported(name = NAME)
    public String getName() {
        return endpoint.getName();
    }

    @Exported(name = API_URL)
    public String getApiUrl() {
        return endpoint.getApiUri();
    }

    @Override
    public Link getLink() {
        return parent.rel(getId());
    }
}
