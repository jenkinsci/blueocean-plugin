package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.Extension;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.model.queue.CauseOfBlockage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.factory.BlueRunFactory;
import io.jenkins.blueocean.rest.impl.pipeline.BranchImpl.Branch;
import io.jenkins.blueocean.rest.impl.pipeline.BranchImpl.PullRequest;
import io.jenkins.blueocean.rest.model.BlueChangeSetEntry;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineNodeContainer;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.service.embedded.rest.AbstractRunImpl;
import io.jenkins.blueocean.service.embedded.rest.QueueUtil;
import io.jenkins.blueocean.service.embedded.rest.StoppableRun;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMRevisionAction;
import org.jenkinsci.plugins.workflow.cps.replay.ReplayAction;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.steps.ExecutorStepExecution;
import org.jenkinsci.plugins.workflow.support.steps.input.InputAction;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.Exported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.concurrent.TimeoutException;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_WORKFLOW_RUN;

/**
 * Pipeline Run
 *
 * @author Vivek Pandey
 */
@Capability(JENKINS_WORKFLOW_RUN)
public class PipelineRunImpl extends AbstractRunImpl<WorkflowRun> {
    private static final Logger logger = LoggerFactory.getLogger(PipelineRunImpl.class);
    public PipelineRunImpl(WorkflowRun run, Reachable parent, BlueOrganization organization) {
        super(run, parent, organization);
    }

    @Exported(name = Branch.BRANCH, inline = true)
    public Branch getBranch() {
        return Branch.getBranch(run.getParent());
    }

    @Exported(name = PullRequest.PULL_REQUEST, inline = true)
    public PullRequest getPullRequest() {
        return PullRequest.get(run.getParent());
    }

    @Override
    public BlueRunState getStateObj() {
        InputAction inputAction = run.getAction(InputAction.class);
        try {
            if(inputAction != null && inputAction.getExecutions().size() > 0){
                return BlueRunState.PAUSED;
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.error("Error getting StateObject from execution context: "+e.getMessage(), e);
        }

        // TODO: Probably move this elsewhere - maybe into PipelineNodeContainerImpl?
        boolean isQueued = false;
        boolean isRunning = false;

        String causeOfBlockage = getCauseOfBlockage();
        for (BluePipelineNode n : getNodes()) {
            BlueRunState nodeState = n.getStateObj();
            // Handle cases where there is a previous successful run - PipelineNodeGraphVisitor.union results in a null
            // getStateObj().
            if (nodeState == null) {
                if (causeOfBlockage != null) {
                    isQueued = true;
                } else {
                    isRunning = true;
                }
            } else if (nodeState.equals(BlueRunState.QUEUED)) {
                isQueued = true;
            } else if (nodeState.equals(BlueRunState.RUNNING)) {
                isRunning = true;
            }
        }

        if (!isRunning && (isQueued || causeOfBlockage != null)) {
            // This would mean we're explicitly queued or we have no running nodes but do have a cause of blockage,
            // which works out the same..
            return BlueRunState.QUEUED;
        }

        return super.getStateObj();
    }

    @Override
    public BlueRun replay() {
        ReplayAction replayAction = run.getAction(ReplayAction.class);
        if(!isReplayable(replayAction)) {
            throw new ServiceException.BadRequestException("This run does not support replay");
        }

        Queue.Item item = replayAction.run2(replayAction.getOriginalScript(), replayAction.getOriginalLoadedScripts());

        if(item == null){
            throw new ServiceException.UnexpectedErrorException("Run was not added to queue.");
        }

        BlueQueueItem queueItem = QueueUtil.getQueuedItem(this.organization, item, run.getParent());
        WorkflowRun replayedRun = QueueUtil.getRun(run.getParent(), item.getId());
        if (queueItem != null) { // If the item is still queued
            return queueItem.toRun();
        } else if (replayedRun != null) { // If the item has left the queue and is running
            return new PipelineRunImpl(replayedRun, parent, organization);
        } else { // For some reason could not be added to the queue
            throw new ServiceException.UnexpectedErrorException("Run was not added to queue.");
        }
    }

    @Override
    public boolean isReplayable() {
        ReplayAction replayAction = run.getAction(ReplayAction.class);
        return isReplayable(replayAction);
    }

    private boolean isReplayable(ReplayAction replayAction) {
        return replayAction != null && replayAction.isRebuildEnabled();
    }

    @Override
    @Navigable
    public BluePipelineNodeContainer getNodes() {
        if (run != null) {
            return new PipelineNodeContainerImpl(run, getLink());
        }
        return null;
    }

    @Override
    @Navigable
    public BluePipelineStepContainer getSteps() {
        return new PipelineStepContainerImpl(run, getLink());
    }

    @Override
    public BlueRun stop(@QueryParameter("blocking") Boolean blocking, @QueryParameter("timeOutInSecs") Integer timeOutInSecs){
        return stop(blocking, timeOutInSecs, new StoppableRun() {
            @Override
            public void stop() {
                run.doStop();
            }
        });
    }


    @Exported(name = "commitId")
    public String getCommitId() {
        SCMRevisionAction data = run.getAction(SCMRevisionAction.class);
        if (data != null){
            return data.getRevision().toString();
        }
        return null;
    }

    @Exported(name = "commitUrl")
    public String getCommitUrl() {
        String commitId = getCommitId();
        if (commitId != null) {
            Container<BlueChangeSetEntry> changeSets = getChangeSet();
            BlueChangeSetEntry entry = changeSets.get(commitId);
            if (entry != null) {
                return entry.getUrl();
            }
        }
        return null;
    }

    @Override
    public String getCauseOfBlockage() {
        for(Queue.Item i: Jenkins.getInstance().getQueue().getItems()) {
            if (i.task instanceof ExecutorStepExecution.PlaceholderTask) {
                ExecutorStepExecution.PlaceholderTask task = (ExecutorStepExecution.PlaceholderTask) i.task;
                Run r = task.runForDisplay();
                if (r != null && r.equals(run)) {
                    String cause = i.getCauseOfBlockage().getShortDescription();
                    CauseOfBlockage causeOfBlockage = task.getCauseOfBlockage();
                    if ( causeOfBlockage != null) {
                        return causeOfBlockage.getShortDescription();
                    }
                    return cause;
                }
            }
        }
        return null;
    }

    @Extension(ordinal = 1)
    public static class FactoryImpl extends BlueRunFactory {

        @Override
        public BlueRun getRun( Run run, Reachable parent, BlueOrganization organization) {
            if(run instanceof WorkflowRun) {
                return new PipelineRunImpl((WorkflowRun) run, parent, organization);
            }
            return null;
        }
    }

    static final Comparator<BlueRun> LATEST_RUN_START_TIME_COMPARATOR = (o1, o2) -> {
            Long t1 = (o1 != null  && o1.getStartTime() != null) ? o1.getStartTime().getTime() : 0;
            Long t2 = (o2 != null  && o2.getStartTime() != null) ? o2.getStartTime().getTime() : 0;
            return t2.compareTo(t1);
        };

}
