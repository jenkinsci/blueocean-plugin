package io.jenkins.blueocean.api.pipeline;

import io.jenkins.blueocean.api.pipeline.model.Pipeline;
import io.jenkins.blueocean.api.pipeline.model.Run;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Service to manage pipeline.
 *
 * @author Vivek Pandey
 * @see Pipeline
 * @see Run
 */
public interface PipelineService {

    /**
     * Find a pipeline for given pipelineName and organizationName
     *
     * @param pipelineRequest pipeline request
     * @return Pipeline Pipeline if found otherwise null
     */
    public
    @Nullable
    GetPipelineResponse getPipeline(@Nonnull GetPipelineRequest pipelineRequest);

    /**
     * Find pipelines for a given organization
     *
     * @return List of pipelines
     */
    public
    @Nonnull
    FindPipelinesResponse findPipelines(@Nonnull FindPipelinesRequest findPipelinesRequest);


    /**
     * Find pipeline run.
     *
     * @param request pipeline request object
     * @return {@link FindPipelineRunsResponse} instance, always non-null
     */
    public
    @Nonnull
    GetPipelineRunResponse getPipelineRun(@Nonnull GetPipelineRunRequest request);

    /**
     * Find pipeline runs.
     *
     * @param request pipeline request object
     * @return {@link FindPipelineRunsResponse} instance, always non-null
     */
    public
    @Nonnull
    FindPipelineRunsResponse findPipelineRuns(@Nonnull FindPipelineRunsRequest request);




}
