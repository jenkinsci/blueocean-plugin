package io.jenkins.blueocean.rest.model;

/**
 * BORun API
 *
 * @author Vivek Pandey
 */
public abstract class BORunContainer extends Container<BORun> {
    /**
     *
     * @param name pipeline name
     * @return pipeline with the given name as parameter
     */
    public abstract BOPipeline getPipeline(String name);
}
