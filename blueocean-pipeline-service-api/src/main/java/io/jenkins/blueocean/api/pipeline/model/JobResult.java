package io.jenkins.blueocean.api.pipeline.model;

/**
 * Describes Job result
 *
 * @author Vivek Pandey
 */
public class JobResult implements Result {
    @Override
    public Type getType() {
        return Type.JOB;
    }

    //TODO: to be filled later based on how BO UI evolves
}
