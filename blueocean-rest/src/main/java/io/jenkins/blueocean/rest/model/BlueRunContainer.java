package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;

/**
 * BlueRun API
 *
 * @author Vivek Pandey
 */
public abstract class BlueRunContainer extends Container<BlueRun> {
    private final Reachable parent;
    protected BlueRunContainer(Reachable parent){
        this.parent = parent;
    }
    /**
     *
     * @param name pipeline name
     * @return pipeline with the given name as parameter
     */
    public abstract BluePipeline getPipeline(String name);

    @Override
    public Link getLink() {
        return parent.getLink().rel("runs");
    }
}
