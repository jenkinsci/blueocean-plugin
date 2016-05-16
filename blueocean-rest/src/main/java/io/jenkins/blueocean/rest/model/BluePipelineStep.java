package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;

import java.util.List;

/**
 * Pipeline Step resource
 *
 * @author Vivek Pandey
 */
public abstract class BluePipelineStep extends BluePipelineNode{

    @Override
    @Exported(skipNull = true)
    public List<Edge> getEdges() {
        return null;
    }
}
