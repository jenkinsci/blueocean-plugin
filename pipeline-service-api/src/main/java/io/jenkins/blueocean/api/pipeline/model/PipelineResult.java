package io.jenkins.blueocean.api.pipeline.model;

/**
 * Abstract to describe Pipeline/workflow build result
 *
 * @author Vivek Pandey
 */
public class PipelineResult implements Result {
    @Override
    public Type getType() {
        return null;
    }

    //TODO: to be filled later based on how BO UI evolves
}
