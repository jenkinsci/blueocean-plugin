package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.hal.Links;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.verb.GET;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.RESOURCE;

/**
 * Stapler-bound object that defines REST endpoint.
 *
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
@Capability(RESOURCE)
public abstract class Resource implements Reachable{
    /**
     * Returns the DTO object that gets databound to Json/XML etc. for state transfer
     *
     * @return DTO object
     */
    @WebMethod(name="") @GET
    @TreeResponse /* this annotation does the above new Api(...).toJson(...) */
    public Object getState() {
        return this;
    }

    /**
     * Links to all resources available in the context of this resource
     *
     * @return {@link Links} object
     */
    @Exported(name = "_links", visibility = 9999)
    public Links getLinks(){
        return new Links(this);
    }
}
