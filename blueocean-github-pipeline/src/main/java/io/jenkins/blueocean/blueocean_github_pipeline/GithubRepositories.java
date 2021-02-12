package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmRepositories;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmRepository;
import org.kohsuke.github.GHRepository;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class GithubRepositories extends ScmRepositories {

    private static final CollectionType GH_REPO_EX_LIST_TYPE = GithubScm.getMappingObjectReader().getTypeFactory().constructCollectionType(List.class, GHRepository.class);

    private final Link self;
    private final List<GHRepository> repositories;
    private final String accessToken;
    private final Integer nextPage;
    private final Integer lastPage;
    private final int pageSize;
    private final StandardUsernamePasswordCredentials credential;
    private final GithubRepositoryContainer parent;


    public GithubRepositories(StandardUsernamePasswordCredentials credentials, String orgUrl, GithubRepositoryContainer parent) {
        this.self = parent.getLink().rel("repositories");
        this.accessToken = credentials.getPassword().getPlainText();
        this.credential = credentials;
        this.parent = parent;

        StaplerRequest request = Stapler.getCurrentRequest();
        int pageNumber = 0;
        if (request.getParameter("pageNumber") != null) {
            pageNumber = Integer.parseInt(request.getParameter("pageNumber"));
        }
        int pageSize = 0;
        if (request.getParameter("pageSize") != null) {
            pageSize = Integer.parseInt(request.getParameter("pageSize"));
        }
        try {
            if (pageNumber == 0) {
                pageNumber = 1; //default
            }
            if (pageSize == 0) {
                pageSize = 100;
            }

            HttpURLConnection connection;
            connection = GithubScm.connect(String.format("%s/repos?type=%s&per_page=%s&page=%s",
                    orgUrl,
                    parent.getRepoType(),
                    pageSize, pageNumber), accessToken);

            this.repositories = GithubScm.getMappingObjectReader().forType(GH_REPO_EX_LIST_TYPE)
                .readValue(HttpRequest.getInputStream(connection));

            String link = connection.getHeaderField("Link");

            int nextPage = 0;
            int lastPage = 0;

            if (link != null) {
                for (String token : link.split(", ")) {
                    if (token.endsWith("rel=\"next\"") || token.endsWith("rel=\"last\"")) {

                        // <https://api.github.com/repos?page=3&per_page=100>; rel="next"
                        // <https://api.github.com/repos?page=3&per_page=100>; rel="next"
                        int idx = token.indexOf('>');
                        URL url = new URL(token.substring(1, idx));
                        for (String q : url.getQuery().split("&")) {
                            if (q.trim().startsWith("page=")) {
                                int i = q.indexOf('=');
                                if (q.length() >= i + 1) {
                                    if (token.endsWith("rel=\"next\"")) {
                                        nextPage = Integer.parseInt(q.substring(i + 1));
                                    }
                                    if (token.endsWith("rel=\"last\"")) {
                                        lastPage = Integer.parseInt(q.substring(i + 1));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            this.nextPage = nextPage > 0 ? nextPage : null;
            this.lastPage = lastPage > 0 ? lastPage : null;
            this.pageSize = pageSize;
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage(), e);
        }
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public Iterable<ScmRepository> getItems() {
        return Iterables.transform(repositories, new Function<GHRepository, ScmRepository>() {
            @Override
            public ScmRepository apply(@Nullable GHRepository input) {
                if(input == null){
                    return null;
                }
                return new GithubRepository(input, credential, GithubRepositories.this);
            }
        });
    }

    @Exported
    public Integer getNextPage(){
        return nextPage;
    }

    @Exported
    public Integer getLastPage(){
        return lastPage;
    }

    @Exported
    public Integer getPageSize(){
        return pageSize;
    }
}
