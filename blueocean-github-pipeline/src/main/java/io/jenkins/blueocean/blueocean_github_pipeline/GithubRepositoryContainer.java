package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmRepositories;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmRepository;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmRepositoryContainer;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * @author Vivek Pandey
 */
public class GithubRepositoryContainer extends ScmRepositoryContainer {
    private final Link self;
    private final StandardUsernamePasswordCredentials credentials;
    private final String rootUrl;
    private final String orgId;
    private final String orgUrl;
    private final String repoType;

    public GithubRepositoryContainer(Scm scm, String orgUrl, String orgId, StandardUsernamePasswordCredentials credentials, Reachable parent) {
        this(scm, orgUrl, orgId, "all", credentials, parent);
    }

    public GithubRepositoryContainer(Scm scm, String orgUrl, String orgId, String repoType, StandardUsernamePasswordCredentials credentials, Reachable parent) {
        this.rootUrl = scm.getUri();
        this.self = parent.getLink().rel("repositories");
        this.credentials = credentials;
        this.orgId = orgId;
        this.orgUrl = orgUrl;
        this.repoType = repoType;
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public ScmRepositories getRepositories() {
        return new GithubRepositories(credentials, orgUrl, this);
    }

    String getRepoType(){
        return repoType;
    }

    @Override
    public ScmRepository get(String name) {
        try {
            HttpURLConnection connection = GithubScm.connect(String.format("%s/repos/%s/%s", rootUrl,
                    orgId,name),credentials.getPassword().getPlainText());
            final GHRepository repository = GithubScm.getMappingObjectReader().forType(GHRepository.class)
                .readValue(HttpRequest.getInputStream(connection));
            return new GithubRepository(repository, credentials, this);
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage(),e);
        }

    }
}
