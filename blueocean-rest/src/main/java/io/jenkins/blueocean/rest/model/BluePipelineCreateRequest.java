package io.jenkins.blueocean.rest.model;

import javax.annotation.Nonnull;

/**
 * Pipeline create request.
 *
 * @author Vivek Pandey
 */
public class BluePipelineCreateRequest {

    private String name;

    private String creatorId;

    private BlueScmConfig scmConfig;

    /**
     * Gives name of the new plugin
     */
    public @Nonnull String getName() {
        return name;
    }

    /**
     * Set name of the pipeline
     *
     * @param name name of the pipeline, always non-null
     */
    public void setName(@Nonnull String name) {
        this.name = name;
    }


    /**
     *
     * Mode of the new plugin to be created. It must be the id of factory
     * implementation that can create inctance of this pipeline
     *
     * @return id of pipeline creator class
     * @see BluePipelineCreator#getId()
     */
    public @Nonnull String getCreatorId() {
        return creatorId;
    }

    /**
     * Id of the pipeline creator class
     *
     * @param creatorId id of pipeline creator class
     * @see BluePipelineCreator#getId()
     */
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * Gives SCM configuration {@link BlueScmConfig}
     */
    public @Nonnull BlueScmConfig getScmConfig() {
        return scmConfig;
    }

    public void setScmConfig(@Nonnull BlueScmConfig scmConfig) {
        this.scmConfig = scmConfig;
    }
}
