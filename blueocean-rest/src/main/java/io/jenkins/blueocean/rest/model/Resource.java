package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.hal.Links;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.verb.GET;

/**
 * Stapler-bound object that defines REST endpoint.
 *
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public abstract class Resource {
    private final Links links = new Links();
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

    /**
     * Links to all resources available in the context of this resource
     *
     * @return {@link Links} object
     */
    @Exported(name = "_links", inline = true)
    public Links getLinks(){
        return links;
    }
}
