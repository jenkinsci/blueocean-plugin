package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.rest.sandbox.BOBranch;
import io.jenkins.blueocean.rest.sandbox.BOBranchContainer;
import io.jenkins.blueocean.rest.sandbox.BOPipeline;

import java.util.Iterator;

/**
 * @author Vivek Pandey
 */
public class BranchContainerImpl extends BOBranchContainer {
    private final BOPipeline pipeline;

    public BranchContainerImpl(BOPipeline pipeline) {
        this.pipeline = pipeline;
    }

    //TODO: implement rest of the methods
    @Override
    public BOBranch get(String name) {
        return null;
    }

    @Override
    public Iterator<BOBranch> iterator() {
        return null;
    }
}
