package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.stapler.TreeResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.POST;

/**
 * BlueRun API
 *
 * @author Vivek Pandey
 */
public abstract class BlueRunContainer extends Container<BlueRun> {
    /**
     *
     * @param name pipeline name
     * @return pipeline with the given name as parameter
     */
    public abstract BluePipeline getPipeline(String name);

    @POST
    @WebMethod(name = "")
    @TreeResponse
    public abstract BlueQueueItem create();
}
