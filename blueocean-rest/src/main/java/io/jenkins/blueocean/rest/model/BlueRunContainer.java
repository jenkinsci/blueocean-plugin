package io.jenkins.blueocean.rest.model;

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
}
