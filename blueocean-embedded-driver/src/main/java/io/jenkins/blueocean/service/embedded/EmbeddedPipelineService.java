package io.jenkins.blueocean.service.embedded;

import hudson.Extension;
import hudson.model.Project;
import hudson.model.Result;
import hudson.util.RunList;
import io.jenkins.blueocean.api.pipeline.FindPipelineRunsRequest;
import io.jenkins.blueocean.api.pipeline.FindPipelineRunsResponse;
import io.jenkins.blueocean.api.pipeline.FindPipelinesRequest;
import io.jenkins.blueocean.api.pipeline.FindPipelinesResponse;
import io.jenkins.blueocean.api.pipeline.GetPipelineRequest;
import io.jenkins.blueocean.api.pipeline.GetPipelineResponse;
import io.jenkins.blueocean.api.pipeline.GetPipelineRunRequest;
import io.jenkins.blueocean.api.pipeline.GetPipelineRunResponse;
import io.jenkins.blueocean.api.pipeline.PipelineService;
import io.jenkins.blueocean.api.pipeline.model.JobResult;
import io.jenkins.blueocean.api.pipeline.model.Pipeline;
import io.jenkins.blueocean.api.pipeline.model.Run;
import io.jenkins.blueocean.security.Identity;
import io.jenkins.blueocean.commons.ServiceException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * {@link PipelineService} implementation to be used embedded as plugin
 *
 * @author Vivek Pandey
 */
public class EmbeddedPipelineService extends AbstractEmbeddedService implements PipelineService {

    @Nonnull
    @Override
    public GetPipelineResponse getPipeline(@Nonnull Identity identity, @Nonnull GetPipelineRequest pipelineRequest) {
        validateOrganization(pipelineRequest.organization);

        List<Project> projects = jenkins.getAllItems(Project.class);
        for (Project project : projects) {
            if (project.getName().equals(pipelineRequest.pipeline)) {
                return new GetPipelineResponse(new Pipeline(pipelineRequest.organization, project.getName(),
                        Collections.<String>emptyList()));
            }
        }

        throw new ServiceException.NotFoundException(String.format("Pipeline %s not found", pipelineRequest.pipeline));
    }

    @Nonnull
    @Override
    public FindPipelinesResponse findPipelines(@Nonnull Identity identity, @Nonnull FindPipelinesRequest findPipelinesRequest) {
        validateOrganization(findPipelinesRequest.organization);

        List<Project> projects = jenkins.getAllItems(Project.class);
        List<Pipeline> pipelines = new ArrayList<Pipeline>();
        for (Project project : projects) {
            if (findPipelinesRequest.pipeline != null &&
                    project.getName().contains(findPipelinesRequest.pipeline)) {

                pipelines.add(new Pipeline(findPipelinesRequest.organization, project.getName(),
                        Collections.EMPTY_LIST));
            }
        }
        return new FindPipelinesResponse(pipelines);
    }

    @Nonnull
    @Override
    public GetPipelineRunResponse getPipelineRun(@Nonnull Identity identity, @Nonnull GetPipelineRunRequest request) {
        validateOrganization(request.organization);

        List<Project> projects = jenkins.getAllItems(Project.class);
        for (Project p : projects) {
            if (!p.getName().equals(request.pipeline)) {
                continue;
            }
            RunList<? extends hudson.model.Run> runList = p.getBuilds();

            return new GetPipelineRunResponse(
                    createBoRun(runList.getLastBuild(), request.organization, request.pipeline));
        }
        throw new ServiceException.NotFoundException(String.format("No run found for organization %s, pipeline: %s",
                request.organization, request.pipeline));

    }

    @Nonnull
    @Override
    public FindPipelineRunsResponse findPipelineRuns(@Nonnull Identity identity, @Nonnull FindPipelineRunsRequest request) {
        validateOrganization(request.organization);

        List<Run> runs = new ArrayList<Run>();
        List<Project> projects = jenkins.getAllItems(Project.class);
        for (Project p : projects) {
            if (request.pipeline != null && !p.getName().equals(request.pipeline)) {
                continue;
            }

            if (request.latestOnly) {
                hudson.model.Run r = p.getLastBuild();
                if(r != null) {
                    Run run = createBoRun(r, request.organization, p.getName());
                    runs.add(run);
                }else{
                    return new FindPipelineRunsResponse(runs,null, null);
                }
            } else {
                RunList<? extends hudson.model.Run> runList = p.getBuilds();

                Iterator<? extends hudson.model.Run> iterator = runList.iterator();
                while (iterator.hasNext()) {
                    runs.add(createBoRun(iterator.next(), request.organization, p.getName()));
                }
            }
        }
        return new FindPipelineRunsResponse(runs, null, null);
    }

    private  Run createBoRun(hudson.model.Run r, String organization, String pipeline) {
        Date endTime = null;
        if (!r.isBuilding()) {
            endTime = new Date(r.getStartTimeInMillis() + r.getDuration());
        }

        return new Run.Builder(r.getId(), pipeline, organization)
                .startTime(new Date(r.getStartTimeInMillis()))
                .enQueueTime(new Date(r.getTimeInMillis()))
                .endTime(endTime)
                .durationInMillis(r.getDuration())
                .status(getStatusFromJenkinsRun(r))
                .runSummary(r.getBuildStatusSummary().message)
                //TODO: need to determine how to check if it's pipeline build
                .result(new JobResult())
                .build();
    }

    private Run.Status getStatusFromJenkinsRun(hudson.model.Run r){
        Result result = r.getResult();
        if(result == null){
            return Run.Status.EXECUTING;
        }
        if (result == Result.SUCCESS) {
            return Run.Status.SUCCESSFUL;
        } else if (result == Result.FAILURE || result == Result.UNSTABLE) {
            return Run.Status.FAILING;
        } else if (!result.isCompleteBuild()) {
            return Run.Status.EXECUTING;
        }else if(r.hasntStartedYet()){
            return Run.Status.IN_QUEUE;
        }else if(result == Result.ABORTED){
            return Run.Status.ABORTED;
        } else if (result == Result.NOT_BUILT){
            return Run.Status.NOT_BUILT;
        }
        return Run.Status.UNKNOWN;
    }

}
