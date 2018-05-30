package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;

public interface BlueManagedSource /* extends BluePipelineItem */ {
    /**
     * @return Gives scm resource attached to this pipeline
     */
    @Navigable
    BluePipelineScm getScm();
}
