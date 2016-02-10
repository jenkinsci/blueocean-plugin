package io.jenkins.blueocean.api.pipeline;

import io.jenkins.blueocean.api.pipeline.model.Pipeline;
import io.jenkins.blueocean.api.pipeline.model.Run;
import io.jenkins.blueocean.security.Identity;

import javax.annotation.Nonnull;

/**
 * Service to manage pipeline.
 *
 * @author Vivek Pandey
 * @see Pipeline
 * @see Run
 */
public interface PipelineService{

    /**
     * Get a pipeline for given pipelineName and organizationName
     *
     * @param identity user identity in this context
     * @param pipelineRequest pipeline request
     * @return {@link GetPipelineResponse} instance
     * @throws io.jenkins.blueocean.commons.ServiceException
     */
    @Nonnull
    GetPipelineResponse getPipeline(@Nonnull Identity identity, @Nonnull GetPipelineRequest pipelineRequest);

    /**
     * Find pipelines for a given organization
     *
     * @param identity user identity in this context
     * @param findPipelinesRequest {@link FindPipelinesRequest} instance
     * @return {@link FindPipelinesResponse} instance
     * @throws io.jenkins.blueocean.commons.ServiceException
     */
    @Nonnull
    FindPipelinesResponse findPipelines(@Nonnull Identity identity, @Nonnull FindPipelinesRequest findPipelinesRequest);


    /**
     * Get pipeline run.
     *
     * @param identity user identity in this context
     * @param request {@link GetPipelineRunRequest} instance
     * @return {@link FindPipelineRunsResponse} instance
     * @throws io.jenkins.blueocean.commons.ServiceException
     */
    @Nonnull
    GetPipelineRunResponse getPipelineRun(@Nonnull Identity identity, @Nonnull GetPipelineRunRequest request);

    /**
     * Find pipeline runs.
     *
     * @param identity user identity in this context
     * @param request {@link FindPipelineRunsRequest} instance
     * @return {@link FindPipelineRunsResponse} instance
     * @throws io.jenkins.blueocean.commons.ServiceException
     */
    @Nonnull
    FindPipelineRunsResponse findPipelineRuns(@Nonnull Identity identity, @Nonnull FindPipelineRunsRequest request);

}
