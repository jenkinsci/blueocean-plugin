package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.rest.model.BlueBranch;
import io.jenkins.blueocean.rest.model.BlueBranchContainer;
import io.jenkins.blueocean.rest.model.BluePipeline;

import java.util.Iterator;

/**
 * @author Vivek Pandey
 */
public class BranchContainerImpl extends BlueBranchContainer {
    private final BluePipeline pipeline;

    public BranchContainerImpl(BluePipeline pipeline) {
        this.pipeline = pipeline;
    }

    //TODO: implement rest of the methods
    @Override
    public BlueBranch get(String name) {
        return null;
    }

    @Override
    public Iterator<BlueBranch> iterator() {
        return null;
    }
}
