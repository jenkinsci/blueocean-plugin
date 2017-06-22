package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.Container;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.github_branch_source.Endpoint;
import org.jenkinsci.plugins.github_branch_source.GitHubConfiguration;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.PUT;
import org.parboiled.common.ImmutableList;
import org.parboiled.common.StringUtils;

import javax.annotation.CheckForNull;
import java.util.Iterator;
import java.util.List;

public class GithubServerContainer extends Container<GithubServer> {

    private final Link parent;

    GithubServerContainer(Link parent) {
        this.parent = parent;
    }

    @PUT
    @WebMethod(name="")
    @TreeResponse
    public @CheckForNull GithubServer create(@JsonBody JSONObject request) {

        List<ErrorMessage.Error> errors = Lists.newLinkedList();

        // Validate name
        final String name = (String) request.get(GithubServer.NAME);
        if (StringUtils.isEmpty(name)) {
            errors.add(new ErrorMessage.Error(GithubServer.NAME, ErrorMessage.Error.ErrorCodes.MISSING.toString(), GithubServer.NAME + " is required"));
        } else {
            GithubServer byName = findByName(name);
            if (byName != null) {
                errors.add(new ErrorMessage.Error(GithubServer.NAME, ErrorMessage.Error.ErrorCodes.ALREADY_EXISTS.toString(), GithubServer.NAME + " already exists for server at '" + byName.getApiUrl() + "'"));
            }
        }

        // Validate url
        final String url = (String) request.get(GithubServer.API_URL);
        if (StringUtils.isEmpty(url)) {
            errors.add(new ErrorMessage.Error(GithubServer.API_URL, ErrorMessage.Error.ErrorCodes.MISSING.toString(), GithubServer.API_URL + " is required"));
        } else {
            GithubServer byUrl = findByURL(url);
            if (byUrl != null) {
                errors.add(new ErrorMessage.Error(GithubServer.API_URL, ErrorMessage.Error.ErrorCodes.ALREADY_EXISTS.toString(), GithubServer.API_URL + " is already registered as '" + byUrl.getName() + "'"));
            }
        }

        if (!errors.isEmpty()) {
            ErrorMessage errorMessage = new ErrorMessage(400, "Failed to create Github server");
            errorMessage.addAll(errors);
            throw new ServiceException.BadRequestException(errorMessage);
        } else {
            GitHubConfiguration config = GitHubConfiguration.get();
            GithubServer server;
            synchronized (config) {
                Endpoint endpoint = new Endpoint(url, name);
                List<Endpoint> endpoints = Lists.newLinkedList(config.getEndpoints());
                endpoints.add(endpoint);
                config.setEndpoints(endpoints);
                config.save();
                server = new GithubServer(endpoint, getLink());
            }
            return server;
        }
     }

    @Override
    public Link getLink() {
        return parent.rel("servers");
    }

    @Override
    public GithubServer get(final String name) {
        GithubServer githubServer = findByName(name);
        if (githubServer == null) {
            throw new ServiceException.NotFoundException("not found");
        }
        return githubServer;
    }

    @Override
    public Iterator<GithubServer> iterator() {
        GitHubConfiguration config = GitHubConfiguration.get();
        ImmutableList<Endpoint> endpoints;
        synchronized (config) {
            endpoints = ImmutableList.copyOf(config.getEndpoints());
        }
        return Iterators.transform(endpoints.iterator(), new Function<Endpoint, GithubServer>() {
            @Override
            public GithubServer apply(Endpoint input) {
                return new GithubServer(input, getLink());
            }
        });
    }

    private GithubServer findByName(final String name) {
        return Iterators.find(iterator(), new Predicate<GithubServer>() {
            @Override
            public boolean apply(GithubServer input) {
                return input.getName().equals(name);
            }
        }, null);
    }

    private GithubServer findByURL(final String url) {
        return Iterators.find(iterator(), new Predicate<GithubServer>() {
            @Override
            public boolean apply(GithubServer input) {
                return input.getApiUrl().equals(url);
            }
        }, null);
    }
}
