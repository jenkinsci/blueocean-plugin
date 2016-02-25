package io.jenkins.blueocean.rest.sandbox;

/**
 * BORun API
 *
 * @author Vivek Pandey
 */
public abstract class RunContainer extends Container<BORun> {
    /** BORun belongs to this Pipeline */
    public abstract Pipeline getPipeline(String name);
}
