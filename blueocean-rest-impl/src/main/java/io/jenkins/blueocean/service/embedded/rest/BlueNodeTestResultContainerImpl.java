package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueTestResultFactory;
import io.jenkins.blueocean.rest.model.BluePipelineNode;

public class BlueNodeTestResultContainerImpl extends BlueTestResultContainerImpl {
    private final BluePipelineNode node;

    public BlueNodeTestResultContainerImpl(Reachable parent, Run<?, ?> run, BluePipelineNode node) {
        super(parent, run);
        this.node = node;
    }

    protected BlueTestResultFactory.Result resolve() {
        return BlueTestResultFactory.resolve(run, node, parent);
    }

}
