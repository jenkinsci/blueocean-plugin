package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.Extension;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.pipeline.api.BlueMultiBranchPipeline.Branch;
import io.jenkins.blueocean.pipeline.api.BlueMultiBranchPipeline.PullRequest;
import io.jenkins.blueocean.pipeline.api.BluePipelineNodeContainer;
import io.jenkins.blueocean.pipeline.api.BluePipelineStepContainer;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.factory.BlueRunFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueChangeSetEntry;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Containers;
import io.jenkins.blueocean.service.embedded.rest.AbstractRunImpl;
import io.jenkins.blueocean.service.embedded.rest.ChangeSetResource;
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

import java.util.LinkedHashMap;
import java.util.Map;
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
    public PipelineRunImpl(WorkflowRun run, Link parent) {
        super(run, parent);
    }

    /**
     * @return branch metadata of run
     */
    @Exported(name = Branch.BRANCH, inline = true)
    public Branch getBranch() {
        return Branch.getBranch(run.getParent());
    }

    /**
     * @return pull request metadata of run or null if run isn't of a pull request
     */
    @Exported(name = PullRequest.PULL_REQUEST, inline = true)
    public PullRequest getPullRequest() {
        return PullRequest.get(run.getParent());
    }

    @Override
    public Container<BlueChangeSetEntry> getChangeSet() {
        Map<String, BlueChangeSetEntry> m = new LinkedHashMap<>();
        int cnt = 0;
        for (ChangeLogSet<? extends Entry> cs : run.getChangeSets()) {
            for (ChangeLogSet.Entry e : cs) {
                cnt++;
                String id = e.getCommitId();
                if (id == null) id = String.valueOf(cnt);
                m.put(id, new ChangeSetResource(e, this));
            }
        }
        return Containers.fromResourceMap(getLink(),m);
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
        return super.getStateObj();
    }

    @Override
    public BlueRun replay() {
        ReplayAction replayAction = run.getAction(ReplayAction.class);
        if(!isReplayable(replayAction)) {
            throw new ServiceException.BadRequestExpception("This run does not support replay");
        }

        Queue.Item item = replayAction.run2(replayAction.getOriginalScript(), replayAction.getOriginalLoadedScripts());

        if(item == null){
            throw new ServiceException.UnexpectedErrorException("Run was not added to queue.");
        }

        BlueQueueItem queueItem = QueueUtil.getQueuedItem(item, run.getParent());
        WorkflowRun replayedRun = QueueUtil.getRun(run.getParent(), item.getId());
        if (queueItem != null) { // If the item is still queued
            return queueItem.toRun();
        } else if (replayedRun != null) { // If the item has left the queue and is running
                return new PipelineRunImpl(replayedRun, parent);
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
        return replayAction != null && replayAction.isEnabled();
    }

    /**
     * @return Serves .../runs/{rundId}/nodes/ and provides pipeline execution nodes
     * @see io.jenkins.blueocean.pipeline.api.BluePipelineNode
     */
    @Navigable
    public BluePipelineNodeContainer getNodes() {
        if (run != null) {
            return new PipelineNodeContainerImpl(run, getLink());
        }
        return null;
    }

    /**
     * @return Gives steps from pipeline. The list of steps must not include stages, this is because stage could be
     * interpreted as step as its StepAtomNode and implementation of this API must ensure not to include it.
     */
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

    @Override
    public String getCauseOfBlockage() {
        for(Queue.Item i: Jenkins.getInstance().getQueue().getItems()) {
            if (i.task instanceof ExecutorStepExecution.PlaceholderTask) {
                ExecutorStepExecution.PlaceholderTask task = (ExecutorStepExecution.PlaceholderTask) i.task;
                Run r = task.runForDisplay();
                if (r != null && r.equals(run)) {
                    String cause = i.getCauseOfBlockage().getShortDescription();
                    if (task.getCauseOfBlockage() != null) {
                        cause = task.getCauseOfBlockage().getShortDescription();
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
        public BlueRun getRun(Run run, Reachable parent) {
            if(run instanceof WorkflowRun) {
                return new PipelineRunImpl((WorkflowRun) run, parent.getLink());
            }
            return null;
        }
    }

}
