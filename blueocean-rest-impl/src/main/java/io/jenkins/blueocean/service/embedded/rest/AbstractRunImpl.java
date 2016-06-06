package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Action;
import hudson.model.FreeStyleBuild;
import hudson.model.Run;
import hudson.plugins.git.util.BuildData;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BluePipelineNodeContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Containers;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.export.Exported;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic {@link BlueRun} implementation.
 *
 * @author Vivek Pandey
 */
public class AbstractRunImpl<T extends Run> extends BlueRun {
    protected final T run;

    public AbstractRunImpl(T run) {
        this.run = run;
    }

    //TODO: It serializes jenkins Run model children, enable this code after fixing it
//    /**
//     * Allow properties reachable through {@link Run} to be exposed upon request (via the tree parameter).
//     */
//    @Exported
//    public T getRun() {
//        return run;
//    }

    /**
     * Subtype should return
     */
    public Container<?> getChangeSet() {
        return null;
    }

    @Override
    public String getOrganization() {
        return OrganizationImpl.INSTANCE.getName();
    }

    @Override
    public String getId() {
        return run.getId();
    }

    @Override
    public String getPipeline() {
        return run.getParent().getName();
    }

    @Override
    public Date getStartTime() {
        return new Date(run.getStartTimeInMillis());
    }

    @Override
    public Date getEnQueueTime() {
        return new Date(run.getTimeInMillis());
    }

    @Override
    public BlueRunState getStateObj() {
        if(!run.hasntStartedYet() && run.isLogUpdated()) {
            return BlueRunState.RUNNING;
        } else if(!run.isLogUpdated()){
            return BlueRunState.FINISHED;
        } else {
            return BlueRunState.QUEUED;
        }
    }

    @Override
    public BlueRunResult getResult() {
        return run.getResult() != null ? BlueRunResult.valueOf(run.getResult().toString()) : BlueRunResult.UNKNOWN;
    }


    @Override
    public Date getEndTime() {
        if (!run.isBuilding()) {
            return new Date(run.getStartTimeInMillis() + run.getDuration());
        }
        return null;
    }

    @Override
    public Long getDurationInMillis() {
        return run.getDuration();
    }

    @Override
    public Long getEstimatedDurtionInMillis() {
        return run.getEstimatedDuration();
    }

    @Override
    public String getRunSummary() {
        return run.getBuildStatusSummary().message;
    }

    @Override
    public String getType() {
        return run.getClass().getSimpleName();
    }

    @Override
    public Object getLog() {
        return new LogResource(run.getLogText());
    }

    @Override
    public Container<BlueArtifact> getArtifacts() {
        Map<String, BlueArtifact> m = new HashMap<String, BlueArtifact>();
        List<Run.Artifact> artifacts = run.getArtifacts();
        for (final Run.Artifact artifact: artifacts) {
            m.put(artifact.getFileName(), new BlueArtifact() {
                @Override
                public String getName() {
                    return artifact.getFileName();
                }

                @Override
                public String getUrl() {
                    return Stapler.getCurrentRequest().getContextPath() +
                        "/" + run.getUrl()+"artifact/"+ artifact.getHref();
                }

                @Override
                public long getSize() {
                    try {
                        return artifact.getFileSize();
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
            });
        }
        return Containers.fromResourceMap(m);
    }

    @Override
    public BluePipelineNodeContainer getNodes() {
        return null; // default
    }

    @Override
    public Collection<?> getActions() {
        List<BlueActionProxy> actionProxies = new ArrayList<>();
        for(Action action:run.getAllActions()){
            actionProxies.add(new ActionProxiesImpl(action));
        }
        return actionProxies;
    }

    protected static BlueRun getBlueRun(Run r){
        //TODO: We need to take care several other job types
        if (r instanceof FreeStyleBuild) {
            return new FreeStyleRunImpl((FreeStyleBuild)r);
        }else if(r instanceof WorkflowRun){
            return new PipelineRunImpl((WorkflowRun)r);
        }else{
            return new AbstractRunImpl<>(r);
        }
    }

    @Exported(name = "commitId")
    public String getCommitId(){
        BuildData data = run.getAction(BuildData.class);

        if(data == null){
            return null;
        } else {
            return data.getLastBuiltRevision().getSha1String();
        }
    }

    @Override
    public BlueRunStopResponse stop() {
        throw new ServiceException.NotImplementedException("Stop should be implemented on a subclass");
    }

    /**
     * Handles HTTP path handled by actions or other extensions
     *
     * @param token path token that an action or extension can handle
     *
     * @return action or extension that handles this path.
     */
    public Object getDynamic(String token) {
        for (Action a : run.getAllActions()) {
            if (token.equals(a.getUrlName()))
                return a;
        }

        return null;
    }

    //XXX: Each action should provide their own link

//    @Override
//    public Links getLinks() {
//        Links links = super.getLinks();
//        for (Action a : run.getAllActions()) {
//            if (a.getUrlName()!=null) {
//                links.add(a.getUrlName());
//            }
//        }
//        return links;
//    }

}
