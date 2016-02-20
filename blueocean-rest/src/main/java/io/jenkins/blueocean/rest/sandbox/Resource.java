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
//    @JsonResponse
//    public void /*getState*/ doIndex(StaplerRequest req, StaplerResponse rsp) {
//        new Api(this).doJson(req,rsp);
//    }
    @TreeResponse /* this annotation does the above new Api(...).doJson(...) */
    public Object /*getState*/ doIndex() {
        return this;
    }
}
