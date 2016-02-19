package io.jenkins.blueocean.rest.sandbox;

import org.kohsuke.stapler.WebMethod;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class Resource {
    /**
     * Returns the DTO object that gets databound to Json/XML etc.
     */
    @WebMethod(name="",method="GET")
    @JsonResponse
    public Object /*getState*/ doIndex() {
        return this;
    }
}
