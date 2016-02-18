package io.jenkins.blueocean.rest.router;

/**
 *
 * Copied from <a href="https://github.com/perwendel/spark">Sinatra inspired Spark</a>
 *
 * @author Per Wendel
 * @author Vivek Pandey
 */
public class RouteMatch {

    private final Route target;
    private final String matchUri;
    private final String requestURI;
    private final String acceptType;

    public RouteMatch(Route target, String matchUri, String requestUri, String acceptType) {
        super();
        this.target = target;
        this.matchUri = matchUri;
        this.requestURI = requestUri;
        this.acceptType = acceptType;
    }

    /**
     * @return the accept type
     */
    public String getAcceptType() {
        return acceptType;
    }

    /**
     * @return the target
     */
    public Route getTarget() {
        return target;
    }


    /**
     * @return the matchUri
     */
    public String getMatchUri() {
        return matchUri;
    }


    /**
     * @return the requestUri
     */
    public String getRequestURI() {
        return requestURI;
    }


}
