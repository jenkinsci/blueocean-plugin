package io.jenkins.blueocean.service.embedded.rest;

/**
 * Run that can be stopped.
 *
 * @author Vivek Pandey
 * @see FreeStyleRunImpl#stop(Boolean, Integer, AbstractRunImpl.StoppableRun)
 */
public interface StoppableRun{

    /**
     * Request stopping a run
     *
     * @throws Exception on error throws exception
     */
    void stop() throws Exception;
}
