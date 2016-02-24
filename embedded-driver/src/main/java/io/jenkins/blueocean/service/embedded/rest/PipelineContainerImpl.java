package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Project;
import hudson.model.TopLevelItem;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.sandbox.Pipeline;
import io.jenkins.blueocean.rest.sandbox.PipelineContainer;
import jenkins.model.Jenkins;

import java.util.Iterator;

/**
 * @author Vivek Pandey
 */
public class PipelineContainerImpl extends PipelineContainer {
    @Override
    public PipelineImpl get(String name) {
        TopLevelItem p = Jenkins.getActiveInstance().getItem(name);
        if (p instanceof Project) {
            return new PipelineImpl((Project)p);
        }

        // TODO: I'm going to turn this into a decorator annotation
        throw new ServiceException.NotFoundException(String.format("Pipeline %s not found", name));
    }

    @Override
    public Iterator<Pipeline> iterator() {
        return null;
    }
}
