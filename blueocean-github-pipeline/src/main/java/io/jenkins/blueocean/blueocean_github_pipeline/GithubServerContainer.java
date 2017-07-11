package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.Container;
import net.sf.json.JSONObject;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github_branch_source.Endpoint;
import org.jenkinsci.plugins.github_branch_source.GitHubConfiguration;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.CheckForNull;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GithubServerContainer extends Container<GithubServer> {

    private static final Logger LOGGER = Logger.getLogger(GithubServerContainer.class.getName());

    private final Link parent;

    GithubServerContainer(Link parent) {
        this.parent = parent;
    }

    @POST
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

        if (StringUtils.isNotEmpty(url)) {
            // Validate that the URL represents a Github API endpoint
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-type", "application/json");
                connection.connect();
                if (connection.getHeaderField("X-GitHub-Request-Id") == null) {
                    errors.add(new ErrorMessage.Error(GithubServer.API_URL, ErrorMessage.Error.ErrorCodes.INVALID.toString(), "Specified URL is not a Github server"));
                }
            } catch (Throwable e) {
                errors.add(new ErrorMessage.Error(GithubServer.API_URL, ErrorMessage.Error.ErrorCodes.INVALID.toString(), e.toString()));
                LOGGER.log(Level.INFO, "Could not connect to Github", e);
            }
        }

        if (!errors.isEmpty()) {
            ErrorMessage errorMessage = new ErrorMessage(400, "Failed to create Github server");
            errorMessage.addAll(errors);
            throw new ServiceException.BadRequestException(errorMessage);
        } else {
            // TODO: this is a temp workaround to facilitate automated Selenium tests
            // since duplicate URLs are not allowed, Selenium appends a random query string param to the API URL
            // this bypasses the uniqueness check but a URL with a query string param will cause downstream errors
            // therefore this method trims off the query string when actually saving the server so a clean URL is used
            // once there is an easy way to delete existing GitHub servers this same logic should be added to
            // validation above
            String sanitizedUrl = discardQueryString(url);
            Endpoint endpoint = new Endpoint(sanitizedUrl, name);
            GitHubConfiguration config = GitHubConfiguration.get();
            config.addEndpoint(endpoint);
            return new GithubServer(endpoint, getLink());
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
        List<Endpoint> endpoints;
        synchronized (config) {
            endpoints = Ordering.from(new Comparator<Endpoint>() {
                @Override
                public int compare(Endpoint o1, Endpoint o2) {
                    return ComparatorUtils.NATURAL_COMPARATOR.compare(o1.getName(), o2.getName());
                }
            }).sortedCopy(config.getEndpoints());
        }
        return Iterators.transform(endpoints.iterator(), new Function<Endpoint, GithubServer>() {
            @Override
            public GithubServer apply(Endpoint input) {
                return new GithubServer(input, getLink());
            }
        });
    }

    private String discardQueryString(String apiUrl) {
        if (apiUrl != null && apiUrl.contains("?")) {
            return apiUrl.substring(0, apiUrl.indexOf("?"));
        }

        return apiUrl;
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
