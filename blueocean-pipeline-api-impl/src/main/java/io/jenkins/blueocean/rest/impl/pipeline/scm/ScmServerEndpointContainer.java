package io.jenkins.blueocean.rest.impl.pipeline.scm;

import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.model.Container;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.POST;

/**
 * Container for {@link ScmServerEndpoint}.
 *
 * @author Vivek Pandey
 */
public abstract class ScmServerEndpointContainer extends Container<ScmServerEndpoint> {
    /**
     * Create {@link ScmServerEndpoint} instance
     *
     * @param request SCM endpoint request
     * @return instance of ScmEndpoint
     */
    @POST
    @WebMethod(name="")
    @TreeResponse
    public abstract ScmServerEndpoint create(@JsonBody JSONObject request);
}
