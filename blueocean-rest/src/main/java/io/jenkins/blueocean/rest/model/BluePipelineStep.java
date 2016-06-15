package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.hal.Link;
import org.kohsuke.stapler.export.Exported;

import java.util.List;

/**
 * Pipeline Step resource
 *
 * @author Vivek Pandey
 */
public abstract class BluePipelineStep extends BluePipelineNode{

    public BluePipelineStep(Link parent) {
        super(parent);
    }

    @Override
    @Exported(skipNull = true)
    public List<Edge> getEdges() {
        return null;
    }
}
