package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.hash.Hashing;
import hudson.security.ACL;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.Container;
import net.sf.json.JSONObject;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github_branch_source.Endpoint;
import org.jenkinsci.plugins.github_branch_source.GitHubConfiguration;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
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
            Endpoint byUrl = GitHubConfiguration.get().findEndpoint(url);
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

        if (errors.isEmpty()) {
            SecurityContext old = null;
            try {
                // We need to escalate privilege to add user defined endpoint to
                old = ACL.impersonate(ACL.SYSTEM);
                GitHubConfiguration config = GitHubConfiguration.get();
                String sanitizedUrl = discardQueryString(url);
                Endpoint endpoint = new Endpoint(sanitizedUrl, name);
                if (!config.addEndpoint(endpoint)) {
                    errors.add(new ErrorMessage.Error(GithubServer.API_URL, ErrorMessage.Error.ErrorCodes.ALREADY_EXISTS.toString(), GithubServer.API_URL + " is already registered as '" + endpoint.getName() + "'"));
                } else {
                    return new GithubServer(endpoint, getLink());
                }
            }finally {
                //reset back to original privilege level
                if(old != null){
                    SecurityContextHolder.setContext(old);
                }
            }
        }
        ErrorMessage message = new ErrorMessage(400, "Failed to create Github server");
        message.addAll(errors);
        throw new ServiceException.BadRequestException(message);
     }

    @Override
    public Link getLink() {
        return parent.rel("servers");
    }

    @Override
    public GithubServer get(final String encodedApiUrl) {
        Endpoint endpoint = Iterables.find(GitHubConfiguration.get().getEndpoints(), new Predicate<Endpoint>() {
            @Override
            public boolean apply(@Nullable Endpoint input) {
                return input != null && encodedApiUrl.equals(Hashing.sha256().hashString(input.getApiUri(), Charsets.UTF_8).toString());
            }
        }, null);
        if (endpoint == null) {
            throw new ServiceException.NotFoundException("not found");
        }
        return new GithubServer(endpoint, getLink());
    }

    @Override
    public Iterator<GithubServer> iterator() {
        List<Endpoint> endpoints = Ordering.from(new Comparator<Endpoint>() {
            @Override
            public int compare(Endpoint o1, Endpoint o2) {
                return ComparatorUtils.NATURAL_COMPARATOR.compare(o1.getName(), o2.getName());
            }
        }).sortedCopy(GitHubConfiguration.get().getEndpoints());
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
}
