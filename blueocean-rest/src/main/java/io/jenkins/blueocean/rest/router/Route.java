package io.jenkins.blueocean.rest.router;

import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Request;
import io.jenkins.blueocean.rest.Response;

import javax.annotation.Nonnull;

/**
 * Any REST resource that can handle an HTTP route gets chance to do it by implementing this interface.
 *
 * Inspired from <a href="https://github.com/perwendel/spark">Sinatra inspired Spark</a>
 *
 * @author Vivek Pandey
 */
public interface Route {

    String DEFAULT_ACCEPT_TYPE = "*/*";
    String DEFAULT_CONTENT_TYPE = "application/json";


    /**
     * Invoked when a request is made on this route's corresponding path e.g. '/hello'
     *
     * @param request  The request object providing information about the HTTP request
     * @param response The response object providing functionality for modifying the response
     * @return The content to be set in the response
     * @throws ServiceException if there is any exception
     */
    Object handle(Request request, Response response);

    /**
     * Gives URL path this Route can handle. e.g. /organizations/:organization-id
     *
     * @return URL path
     */
    @Nonnull String path();

    /**
     * Gives accessType this route can consume. Incoming requests's Access header will be matched against the
     * Route's accessType and if it doesn't match then an error will be generated with 406 Not Acceptable error.
     *
     * @return MIME type the route can
     */
    @Nonnull String accessType();

    /**
     * Content type this route produces
     *
     * @return content type
     */
    String contentType();

    abstract class RouteImpl implements Route{
        private final String path;
        private final String acceptType;
        private final String contentType;

        public RouteImpl(String path) {
            this.path = path;
            this.acceptType = DEFAULT_ACCEPT_TYPE;
            this.contentType = DEFAULT_CONTENT_TYPE;
        }

        public RouteImpl(String path, String acceptType) {
            this.path = path;
            this.acceptType = acceptType;
            this.contentType = DEFAULT_CONTENT_TYPE;
        }

        public RouteImpl(String path, String acceptType, String contentType) {
            this.path = path;
            this.acceptType = acceptType;
            this.contentType = contentType;
        }

        @Override
        public String path() {
            return path;
        }

        @Override
        public String accessType() {
            return acceptType;
        }

        @Override
        public String contentType() {
            return contentType;
        }
    }

}
