package io.jenkins.blueocean.rest.impl.pipeline;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Action;
import hudson.model.Queue;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.Export;
import io.jenkins.blueocean.listeners.NodeDownstreamBuildAction;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueInputStep;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.rest.AbstractRunImpl;
import io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl;
import io.jenkins.blueocean.service.embedded.rest.QueueUtil;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.actions.RestartDeclarativePipelineAction;
import org.jenkinsci.plugins.workflow.actions.LogAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link BluePipelineNode}.
 *
 * @author Vivek Pandey
 * @see FlowNode
 */
public class PipelineNodeImpl extends BluePipelineNode {
    private static final Logger LOGGER = LoggerFactory.getLogger( PipelineNodeImpl.class );
    private final FlowNodeWrapper node;
    private final List<Edge> edges;
    private final Long durationInMillis;
    private final NodeRunStatus status;
    private final Link self;
    private final String runExternalizableId;
    private final Reachable parent;
    public static final int waitJobInqueueTimeout = Integer.getInteger("blueocean.wait.job.inqueue", 1000);

    public PipelineNodeImpl(FlowNodeWrapper node, Reachable parent, WorkflowRun run) {
        this.node = node;
        this.runExternalizableId = run.getExternalizableId();
        this.edges = buildEdges(node.edges);
        this.status = node.getStatus();
        this.durationInMillis = node.getTiming().getTotalDurationMillis();
        this.self = parent.getLink().rel(node.getId());
        this.parent = parent;
    }

    @Override
    public String getId() {
        return node.getId();
    }

    @Override
    public String getDisplayName() {
        return PipelineNodeUtil.getDisplayName(node.getNode());
    }

    @Override
    public String getDisplayDescription() {
        return null;
    }

    @Override
    public BlueRun.BlueRunResult getResult() {
        return status.getResult();
    }

    @Override
    public BlueRun.BlueRunState getStateObj() {
        return status.getState();
    }

    @Override
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public @CheckForNull String getFirstParent() {
        return node.getFirstParent() == null ? null : node.getFirstParent().getId();
    }

    @Override
    public Date getStartTime() {
        long nodeTime = node.getTiming().getStartTimeMillis();
        if (nodeTime == 0) {
            return null;
        }
        return new Date(nodeTime);
    }

    public String getStartTimeString() {
        if (getStartTime() == null) {
            return null;
        }
        return AbstractRunImpl.DATE_FORMAT.format(getStartTime().toInstant());
    }

    @Override
    public List<Edge> getEdges() {
        return edges;
    }

    @Override
    public Long getDurationInMillis() {
        return durationInMillis;
    }

    /**
     * Appended logs of steps.
     *
     * @see BluePipelineStep#getLog()
     */
    @Override
    public Object getLog() {
        return new NodeLogResource(this);
    }

    @Override
    public String getType() {
        return node.getType().name();
    }

    @Override
    public String getStepType() {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public String getCauseOfBlockage() {
        return node.getCauseOfFailure();
    }

    @Override
    public BluePipelineStepContainer getSteps() {
        return new PipelineStepContainerImpl(node, self, this.runExternalizableId);
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public Collection<BlueActionProxy> getActions() {

        HashSet<Action> actions = new HashSet<>();

        // Actions attached to the node we use for the graph
        actions.addAll(node.getNode().getActions());

        // Actions from any child nodes
        actions.addAll(node.getPipelineActions(NodeDownstreamBuildAction.class));

        return ActionProxiesImpl.getActionProxies(actions,
                                                  input -> input instanceof LogAction || input instanceof NodeDownstreamBuildAction,
                                                  this);
    }

    private WorkflowRun getRun() {
        return PipelineRunImpl.findRun(runExternalizableId);
    }

    @Override
    public boolean isRestartable() {
        RestartDeclarativePipelineAction restartDeclarativePipelineAction =
            getRun().getAction( RestartDeclarativePipelineAction.class );
        if (restartDeclarativePipelineAction != null) {
            List<String> restartableStages = restartDeclarativePipelineAction.getRestartableStages();
            if (restartableStages != null) {
                return restartableStages.contains(this.getDisplayName())
                    && this.getStateObj() == BlueRun.BlueRunState.FINISHED;
            }
        }
        return false;
    }

    @Override
    public BlueInputStep getInputStep() {
        return null;
    }

    @Override
    public HttpResponse submitInputStep(StaplerRequest request) {
        return null;
    }

    public HttpResponse restart(StaplerRequest request) {
        try
        {
            WorkflowRun run = getRun();
            JSONObject body = JSONObject.fromObject( IOUtils.toString( request.getReader() ) );
            boolean restart = body.getBoolean( "restart" );
            if ( restart && isRestartable() ) {
                LOGGER.debug( "submitInputStep, restart: {}, step: {}", restart, this.getDisplayName() );

                RestartDeclarativePipelineAction restartDeclarativePipelineAction =
                    run.getAction( RestartDeclarativePipelineAction.class );
                Queue.Item item = restartDeclarativePipelineAction.run( this.getDisplayName() );
                BluePipeline bluePipeline = BluePipelineFactory.getPipelineInstance(run.getParent(), this.parent);
                BlueQueueItem queueItem = QueueUtil.getQueuedItem( bluePipeline.getOrganization(), item, run.getParent());

                if (queueItem != null) { // If the item is still queued
                    return ( req, rsp, node1 ) -> {
                        rsp.setStatus( HttpServletResponse.SC_OK );
                        rsp.getOutputStream().print( Export.toJson( queueItem.toRun() ) );
                    };
                }

                final WorkflowRun restartRun = getRun(run.getParent(), item.getId());
                if (restartRun != null) {
                    return (req, rsp, node1 ) -> {
                        rsp.setStatus( HttpServletResponse.SC_OK );
                        rsp.getOutputStream().print( Export.toJson( new PipelineRunImpl(restartRun, parent,
                                                                                        bluePipeline.getOrganization()) ) );
                    };
                } else { // For some reason could not be added to the queue
                    throw new ServiceException.UnexpectedErrorException("Run was not added to queue.");
                }
            }
            // ISE cant happen if stage not restartable or anything else :)
        } catch ( Exception e) {
            LOGGER.warn( "error restarting stage: " + e.getMessage(), e);
            throw new ServiceException.UnexpectedErrorException( e.getMessage());
        }
        return null;
    }

    protected static WorkflowRun getRun(WorkflowJob job, long itemId) throws InterruptedException {
        WorkflowRun restartRun = QueueUtil.getRun(job, itemId);
        if (restartRun == null) {
            long startTimeMs = System.currentTimeMillis();
            while (restartRun == null && System.currentTimeMillis()-startTimeMs < waitJobInqueueTimeout) {
                Thread.sleep(100);
                restartRun = QueueUtil.getRun(job, itemId);
            }
        }
        return restartRun;
    }

    public static class EdgeImpl extends Edge {
        private final String id;
        private final String type;

        public EdgeImpl(FlowNodeWrapper edge) {
            this.id = edge.getId();
            this.type = edge.getType().name();
        }

        @Override
        public String getId() {
            return id;
        }

        @Exported
        public String getType() {
            return type;
        }
    }

    private List<Edge> buildEdges(List<FlowNodeWrapper> nodes) {
        return nodes.isEmpty()? Collections.emptyList():
            nodes.stream().map(EdgeImpl::new).collect(Collectors.toList());

    }

    FlowNodeWrapper getFlowNodeWrapper() {
        return node;
    }

    @Override
    public String toString() {
        return "PipelineNodeImpl{" + "node=" + node + ", edges=" + edges + ", status=" + status + '}';
    }
}
