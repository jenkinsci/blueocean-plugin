package io.jenkins.blueocean.rest.router;

/**
 * @author Vivek Pandey
 */

import io.jenkins.blueocean.rest.Body;
import io.jenkins.blueocean.rest.HttpMethod;
import io.jenkins.blueocean.rest.Response;

import javax.servlet.http.HttpServletRequest;

/**
 * Holds the parameters needed in the Before filters, Routes and After filters execution.
 */
public final class RouteContext {

    /**
     * Creates a RouteContext
     */
    public static RouteContext create() {
        return new RouteContext();
    }

    private RouteMatcher routeMatcher;
    private HttpServletRequest httpRequest;
    private String uri;
    private String acceptType;
    private Body body;
    private Response response;
    private HttpMethod httpMethod;

    private RouteContext() {
        // hidden
    }

    public RouteMatcher routeMatcher() {
        return routeMatcher;
    }

    public RouteContext withMatcher(RouteMatcher routeMatcher) {
        this.routeMatcher = routeMatcher;
        return this;
    }

    public RouteContext withHttpRequest(HttpServletRequest httpRequest) {
        this.httpRequest = httpRequest;
        return this;
    }

    public RouteContext withAcceptType(String acceptType) {
        this.acceptType = acceptType;
        return this;
    }

    public RouteContext withBody(Body body) {
        this.body = body;
        return this;
    }


    public RouteContext withUri(String uri) {
        this.uri = uri;
        return this;
    }

    public RouteContext withResponse(Response response) {
        this.response = response;
        return this;
    }

    public RouteContext withHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public HttpServletRequest httpRequest() {
        return httpRequest;
    }

    public String uri() {
        return uri;
    }

    public String acceptType() {
        return acceptType;
    }

    public Body body() {
        return body;
    }

    public Response response() {
        return response;
    }

    public HttpMethod httpMethod() {
        return httpMethod;
    }

}
