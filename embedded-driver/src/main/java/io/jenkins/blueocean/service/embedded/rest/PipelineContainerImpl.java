package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Project;
import hudson.model.TopLevelItem;
import io.jenkins.blueocean.api.pipeline.model.Pipeline;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.sandbox.PipelineContainer;
import jenkins.model.Jenkins;

import java.util.Collections;
import java.util.Iterator;

/**
 * @author Vivek Pandey
 */
public class PipelineContainerImpl extends PipelineContainer {
    @Override
    public Pipeline get(String name) {
        TopLevelItem topLevelItem = Jenkins.getActiveInstance().getItem(name);
        if(topLevelItem instanceof Project){

           return new Pipeline(null, name, Collections.<String>emptyList());
        }

        throw new ServiceException.NotFoundException(String.format("Pipeline %s not found", name));

    }

    @Override
    public Iterator<Pipeline> iterator() {
        return null;
    }
}
