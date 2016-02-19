package io.jenkins.blueocean.rest;

import io.jenkins.blueocean.rest.router.Route;

/**
 * Filter is just another Route
 *
 * @author Per Wendel
 * @author Vivek Pandey
 */
public interface Filter extends Route{

    abstract class FilterImpl implements Filter{
        private final String path;
        private final String acceptType;
        private final String contentType;

        public FilterImpl(String path) {
            this.path = path;
            this.acceptType = Route.DEFAULT_ACCEPT_TYPE;
            this.contentType = Route.DEFAULT_CONTENT_TYPE;
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
