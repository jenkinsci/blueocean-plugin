package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.stapler.TreeResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.verb.GET;

/**
 * Stapler-bound object that defines REST endpoint.
 *
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public abstract class Resource {
    /**
     * Returns the DTO object that gets databound to Json/XML etc. for state transfer
     *
     * @return DTO object
     */
    @WebMethod(name="") @GET
    @TreeResponse /* this annotation does the above new Api(...).doJson(...) */
    public Object getState() {
        return this;
    }
}
