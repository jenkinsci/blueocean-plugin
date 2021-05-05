package io.jenkins.blueocean.blueocean_github_pipeline;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Ordering;
import com.google.common.hash.Hashing;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.security.ACLContext;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpoint;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpointContainer;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github_branch_source.Endpoint;
import org.jenkinsci.plugins.github_branch_source.GitHubConfiguration;
import org.kohsuke.stapler.json.JsonBody;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GithubServerContainer extends ScmServerEndpointContainer {

    private static final Logger LOGGER = Logger.getLogger(GithubServerContainer.class.getName());
    static final String ERROR_MESSAGE_INVALID_SERVER = "Specified URL is not a GitHub server; check hostname";
    static final String ERROR_MESSAGE_INVALID_APIURL = "Specified URL is not a GitHub API endpoint; check path";

    private final Link parent;


    GithubServerContainer(Link parent) {
        this.parent = parent;
    }

    public @CheckForNull ScmServerEndpoint create(@JsonBody JSONObject request) {

        try {
            Jenkins.get().checkPermission(Item.CREATE);
        } catch (Exception e) {
            throw new ServiceException.ForbiddenException("User does not have permission to create repository.", e);
        }

        List<ErrorMessage.Error> errors = new LinkedList<>();

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
            // Validate that the URL represents a GitHub API endpoint
            try {
                HttpURLConnection connection = HttpRequest.get(url).connect();

                if (connection.getHeaderField("X-GitHub-Request-Id") == null) {
                    errors.add(new ErrorMessage.Error(GithubServer.API_URL, ErrorMessage.Error.ErrorCodes.INVALID.toString(), ERROR_MESSAGE_INVALID_SERVER));
                } else {
                    boolean isGithubCloud = false;
                    boolean isGithubEnterprise = false;

                    try {
                        InputStream inputStream;
                        int code = connection.getResponseCode();

                        if (200 <= code && code < 300) {
                            inputStream = HttpRequest.getInputStream(connection);
                        } else {
                            inputStream = HttpRequest.getErrorStream(connection);
                        }

                        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>(){};
                        Map<String, String> responseBody = GithubScm.getMappingObjectReader().forType(typeRef).readValue(inputStream);

                        isGithubCloud = code == 200 && responseBody.containsKey("current_user_url");
                        isGithubEnterprise = code == 401 && responseBody.containsKey("message");
                    } catch (IllegalArgumentException | IOException ioe) {
                        LOGGER.log(Level.INFO, "Could not parse response body from Github");
                    }

                    if (!isGithubCloud && !isGithubEnterprise) {
                        errors.add(new ErrorMessage.Error(GithubServer.API_URL, ErrorMessage.Error.ErrorCodes.INVALID.toString(), ERROR_MESSAGE_INVALID_APIURL));
                    }
                }
            } catch (Throwable e) {
                errors.add(new ErrorMessage.Error(GithubServer.API_URL, ErrorMessage.Error.ErrorCodes.INVALID.toString(), e.toString()));
                LOGGER.log(Level.INFO, "Could not connect to Github", e);
            }
        }

        if (errors.isEmpty()) {
            try (ACLContext ctx = ACL.as(ACL.SYSTEM)) {
                // We need to escalate privilege to add user defined endpoint to
                GitHubConfiguration config = GitHubConfiguration.get();
                String sanitizedUrl = discardQueryString(url);
                Endpoint endpoint = new Endpoint(sanitizedUrl, name);
                if (!config.addEndpoint(endpoint)) {
                    errors.add(new ErrorMessage.Error(GithubServer.API_URL, ErrorMessage.Error.ErrorCodes.ALREADY_EXISTS.toString(), GithubServer.API_URL + " is already registered as '" + endpoint.getName() + "'"));
                } else {
                    return new GithubServer(endpoint, getLink());
                }
            }
        }
        ErrorMessage message = new ErrorMessage(400, "Failed to create GitHub server");
        message.addAll(errors);
        throw new ServiceException.BadRequestException(message);
     }

    @Override
    public Link getLink() {
        return parent.rel("servers");
    }

    @Override
    public GithubServer get(final String encodedApiUrl) {
        Endpoint endpoint = Iterables.find(GitHubConfiguration.get().getEndpoints(), input ->
            input != null && encodedApiUrl.equals( Hashing.sha256().hashString( input.getApiUri(), Charsets.UTF_8).toString()), null);
        if (endpoint == null) {
            throw new ServiceException.NotFoundException("not found");
        }
        return new GithubServer(endpoint, getLink());
    }

    @Override
    public Iterator<ScmServerEndpoint> iterator() {
        List<Endpoint> endpoints = Ordering.from((Comparator<Endpoint>) (o1, o2) ->
             ComparatorUtils.NATURAL_COMPARATOR.compare(o1.getName(), o2.getName()))
            .sortedCopy( GitHubConfiguration.get().getEndpoints());
        return Iterators.transform( endpoints.iterator(), input -> new GithubServer(input, getLink()));
    }

    private String discardQueryString(String apiUrl) {
        if (apiUrl != null && apiUrl.contains("?")) {
            return apiUrl.substring(0, apiUrl.indexOf("?"));
        }
        return apiUrl;
    }

    private GithubServer findByName(final String name) {
        return (GithubServer) Iterators.find( iterator(), input -> input.getName().equals( name), null);
    }
}
