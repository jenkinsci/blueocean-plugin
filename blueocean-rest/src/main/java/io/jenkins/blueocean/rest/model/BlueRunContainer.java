package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.stapler.TreeResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.POST;

/**
 * BlueRun API
 *
 * @author Vivek Pandey
 */
public abstract class BlueRunContainer extends Container<BlueRun> {

    @POST
    @WebMethod(name = "")
    @TreeResponse
    public abstract BlueRun create(StaplerRequest request);
}
