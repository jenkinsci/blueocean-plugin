package io.jenkins.blueocean.rest.sandbox;

/**
 * BORun API
 *
 * @author Vivek Pandey
 */
public abstract class BORunContainer extends Container<BORun> {
    /** BORun belongs to this Pipeline */
    public abstract BOPipeline getPipeline(String name);
}
