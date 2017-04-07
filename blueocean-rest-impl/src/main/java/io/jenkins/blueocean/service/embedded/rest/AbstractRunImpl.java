package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Action;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.Links;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueArtifactContainer;
import io.jenkins.blueocean.rest.model.BlueChangeSetEntry;
import io.jenkins.blueocean.rest.model.BluePipelineNodeContainer;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueTestResultContainer;
import io.jenkins.blueocean.rest.model.BlueTestSummary;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.GenericResource;
import org.kohsuke.stapler.QueryParameter;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Basic {@link BlueRun} implementation.
 *
 * @author Vivek Pandey
 */
public class AbstractRunImpl<T extends Run> extends BlueRun {
    protected final T run;

    private final Link parent;
    public AbstractRunImpl(T run, Link parent) {
        this.run = run;
        this.parent = parent;
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
    public Container<BlueChangeSetEntry> getChangeSet() {
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
    public BlueQueueItem replay() {
        return null;
    }

    @Override
    public BlueArtifactContainer getArtifacts() {
       return new ArtifactContainerImpl(run, this);
    }

    @Override
    public BluePipelineNodeContainer getNodes() {
        return null; // default
    }

    @Override
    public BluePipelineStepContainer getSteps() {
        return null;
    }

    @Override
    public BlueTestResultContainer getTests() {
        return new BlueTestResultContainer(this, run);
    }

    @Override
    public BlueTestSummary getTestSummary() {
        List<AbstractTestResultAction> actions = run.getActions(AbstractTestResultAction.class);
        if (actions.isEmpty()) {
            return null;
        }
        long passed = 0;
        long failed = 0;
        long skipped = 0;
        long total = 0;
        for (AbstractTestResultAction action : actions) {
            passed += action.getPassedTests().size();
            failed += action.getFailCount();
            skipped += action.getSkipCount();
            total += action.getTotalCount();
        }
        return new BlueTestSummary(passed, failed, skipped, total, 0);
    }

    public Collection<BlueActionProxy> getActions() {
        return ActionProxiesImpl.getActionProxies(run.getAllActions(), this);
    }

    public static BlueRun getBlueRun(Run r, Reachable parent){
        for(BlueRunFactory runFactory:BlueRunFactory.all()){
            BlueRun blueRun = runFactory.getRun(r,parent);
            if(blueRun != null){
                return blueRun;
            }
        }
        return new AbstractRunImpl<>(r, parent.getLink());
    }

    @Override
    public BlueRun stop(@QueryParameter("blocking") Boolean blocking, @QueryParameter("timeOutInSecs") Integer timeOutInSecs){
        throw new ServiceException.NotImplementedException("Stop should be implemented on a subclass");
    }

    @Override
    public String getArtifactsZipFile() {
        return "/" + run.getUrl()+"artifact/*zip*/archive.zip";
    }

    protected BlueRun stop(Boolean blocking, Integer timeOutInSecs, StoppableRun stoppableRun){
            if(blocking == null){
                blocking = false;
            }
            try {
                long start = System.currentTimeMillis();
                if(timeOutInSecs == null){
                    timeOutInSecs = DEFAULT_BLOCKING_STOP_TIMEOUT_IN_SECS;
                }
                if(timeOutInSecs < 0){
                    throw new ServiceException.BadRequestExpception("timeOutInSecs must be >= 0");
                }

                long timeOutInMillis = timeOutInSecs*1000;

                long sleepingInterval = timeOutInMillis/10; //one tenth of timeout
                do{
                    if(isCompletedOrAborted()){
                        return this;
                    }
                    stoppableRun.stop();
                    if(isCompletedOrAborted()){
                        return this;
                    }
                    Thread.sleep(sleepingInterval);
                }while(blocking && (System.currentTimeMillis() - start) < timeOutInMillis);

            } catch (Exception e) {
                throw new ServiceException.UnexpectedErrorException(String.format("Failed to stop run %s: %s", run.getId(), e.getMessage()), e);
            }
        return this;
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
                return new GenericResource<>(a);
        }

        return null;
    }

    @Override
    public Link getLink() {
        if(parent == null){
            return OrganizationImpl.INSTANCE.getLink().rel(String.format("pipelines/%s/runs/%s", run.getParent().getName(), getId()));
        }
        return parent.rel("runs/"+getId());
    }

    private boolean isCompletedOrAborted(){
        return run.getResult()!= null && (run.getResult() == Result.ABORTED || run.getResult().isCompleteBuild());
    }

    @Override
    public Links getLinks() {
        return super.getLinks().add("parent", parent);
    }
}
