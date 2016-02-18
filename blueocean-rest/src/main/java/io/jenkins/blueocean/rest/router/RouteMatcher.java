package io.jenkins.blueocean.rest.router;

import io.jenkins.blueocean.rest.HttpMethod;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple route matcher that is supposed to work exactly as Sinatra's
 *
 * @author Per Wendel
 * @author Vivek Pandey
 */
/** package */
class RouteMatcher {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RouteMatcher.class);
    private static final char SINGLE_QUOTE = '\'';

    private final List<RouteEntry> routes = new ArrayList<>();

    /**
     * Parse and validates a route and adds it
     *
     * @param target     the invocation target
     */
    public void parseValidateAddRoute(HttpMethod method, Route target) {
        String route = target.path();
        String acceptType = target.accessType();
        addRoute(method, route, acceptType, target);
    }

    /**
     * finds target for a requested route
     *
     * @param httpMethod the http method
     * @param path       the path
     * @param acceptType the accept type
     * @return the target
     */
    public RouteMatch findTargetForRequestedRoute(HttpMethod httpMethod, String path, String acceptType) {
        List<RouteEntry> routeEntries = this.findTargetsForRequestedRoute(httpMethod, path);
        RouteEntry entry = findTargetWithGivenAcceptType(routeEntries, acceptType);
        return entry != null ? new RouteMatch(entry.target, entry.path, path, acceptType) : null;
    }

    /**
     * ¨Clear all routes
     */
    public void clearRoutes() {
        routes.clear();
    }

    /**
     * Removes a particular route from the collection of those that have been previously routed.
     * Search for a previously established routes using the given path and HTTP method, removing
     * any matches that are found.
     *
     * @param path       the route path
     * @param httpMethod the http method
     * @return <tt>true</tt> if this a matching route has been previously routed
     * @throws IllegalArgumentException if <tt>path</tt> is null or blank or if <tt>httpMethod</tt> is null, blank
     *                                  or an invalid HTTP method
     * @since 2.2
     */
    public boolean removeRoute(String path, String httpMethod) {
        if (StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("path cannot be null or blank");
        }

        if (StringUtils.isEmpty(httpMethod)) {
            throw new IllegalArgumentException("httpMethod cannot be null or blank");
        }

        // Catches invalid input and throws IllegalArgumentException
        HttpMethod method = HttpMethod.valueOf(httpMethod);

        return removeRoute(method, path);
    }

    /**
     * Removes a particular route from the collection of those that have been previously routed.
     * Search for a previously established routes using the given path and removes any matches that are found.
     *
     * @param path the route path
     * @return <tt>true</tt> if this a matching route has been previously routed
     * @throws java.lang.IllegalArgumentException if <tt>path</tt> is null or blank
     * @since 2.2
     */
    public boolean removeRoute(String path) {
        if (StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("path cannot be null or blank");
        }

        return removeRoute((HttpMethod) null, path);
    }

    //////////////////////////////////////////////////
    // PRIVATE METHODS
    //////////////////////////////////////////////////

    private void addRoute(HttpMethod method, String url, String acceptedType, Route target) {
        RouteEntry entry = new RouteEntry(method, url, acceptedType, target);
        LOG.debug("Adds route: " + entry);
        // Adds to end of list
        routes.add(entry);
    }

    //can be cached? I don't think so.
    private Map<String, RouteEntry> getAcceptedMimeTypes(List<RouteEntry> routes) {
        Map<String, RouteEntry> acceptedTypes = new HashMap<>();

        for (RouteEntry routeEntry : routes) {
            if (!acceptedTypes.containsKey(routeEntry.acceptedType)) {
                acceptedTypes.put(routeEntry.acceptedType, routeEntry);
            }
        }

        return acceptedTypes;
    }

    private boolean routeWithGivenAcceptType(String bestMatch) {
        return !MimeParser.NO_MIME_TYPE.equals(bestMatch);
    }

    private List<RouteEntry> findTargetsForRequestedRoute(HttpMethod httpMethod, String path) {
        List<RouteEntry> matchSet = new ArrayList<RouteEntry>();
        for (RouteEntry entry : routes) {
            if (entry.matches(httpMethod, path)) {
                matchSet.add(entry);
            }
        }
        return matchSet;
    }

    private RouteEntry findTargetWithGivenAcceptType(List<RouteEntry> routeMatches, String acceptType) {
        if (acceptType != null && routeMatches.size() > 0) {
            Map<String, RouteEntry> acceptedMimeTypes = getAcceptedMimeTypes(routeMatches);
            String bestMatch = MimeParser.bestMatch(acceptedMimeTypes.keySet(), acceptType);

            if (routeWithGivenAcceptType(bestMatch)) {
                return acceptedMimeTypes.get(bestMatch);
            } else {
                return null;
            }
        } else {
            if (routeMatches.size() > 0) {
                return routeMatches.get(0);
            }
        }

        return null;
    }

    private boolean removeRoute(HttpMethod httpMethod, String path) {
        List<RouteEntry> forRemoval = new ArrayList<>();

        for (RouteEntry routeEntry : routes) {
            HttpMethod httpMethodToMatch = httpMethod;

            if (httpMethod == null) {
                // Use the routeEntry's HTTP method if none was given, so that only path is used to match.
                httpMethodToMatch = routeEntry.httpMethod;
            }

            if (routeEntry.matches(httpMethodToMatch, path)) {
                LOG.debug("Removing path {}", path, httpMethod == null ? "" : " with HTTP method " + httpMethod);

                forRemoval.add(routeEntry);
            }
        }

        return routes.removeAll(forRemoval);
    }
}
