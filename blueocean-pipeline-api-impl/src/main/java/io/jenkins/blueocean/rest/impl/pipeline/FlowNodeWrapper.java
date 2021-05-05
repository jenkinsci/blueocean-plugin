package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.Action;
import hudson.model.Result;
import io.jenkins.blueocean.rest.model.BlueRun.BlueRunResult;
import io.jenkins.blueocean.rest.model.BlueRun.BlueRunState;
import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.graph.AtomNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.TimingInfo;
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStep;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class FlowNodeWrapper {

    /**
     * Checks to see if `this` and `that` probably represent the same underlying pipeline graph node as far as the user
     * is concerned. This is sloppier than an exact name and ID match because {@link PipelineNodeGraphVisitor} as of
     * 2019-05-17 can return some nodes with different IDs during a build as opposed to once the build is complete.
     * As such we check name, type, and firstParent. But we need to check firstParent the same way for the same reason.
     *
     * @param that
     * @return
     */
    public boolean probablySameNode(@Nullable FlowNodeWrapper that) {

        if (that == null) {
            return false;
        }

        if (this.type != that.type) {
            return false;
        }

        if (!this.displayName.equals(that.displayName)) {
            return false;
        }

        final FlowNodeWrapper thisParent = this.getFirstParent();
        if (thisParent != null) {
            return thisParent.probablySameNode(that.getFirstParent());
        } else {
            return that.getFirstParent() == null;
        }
    }

    public enum NodeType {STAGE, PARALLEL, STEP}

    private final FlowNode node;
    private final NodeRunStatus status;
    private final TimingInfo timingInfo;
    public final List<FlowNodeWrapper> edges = new ArrayList<>();
    public final NodeType type;
    private final String displayName;
    private final InputStep inputStep;
    private final WorkflowRun run;
    private String causeOfFailure;

    private List<FlowNodeWrapper> parents = new ArrayList<>();

    private ErrorAction blockErrorAction;
    private Collection<Action> pipelineActions;


    public FlowNodeWrapper(@Nonnull FlowNode node, @Nonnull NodeRunStatus status, @Nonnull TimingInfo timingInfo, @Nonnull WorkflowRun run) {
        this(node, status, timingInfo, null, run);
    }

    public FlowNodeWrapper(@Nonnull FlowNode node, @Nonnull NodeRunStatus status,
                           @Nonnull TimingInfo timingInfo, @Nullable InputStep inputStep, @Nonnull WorkflowRun run) {
        this.node = node;
        this.status = status;
        this.timingInfo = timingInfo;
        this.type = getNodeType(node);
        this.displayName = PipelineNodeUtil.getDisplayName(node);
        this.inputStep = inputStep;
        this.run = run;
    }


    public WorkflowRun getRun() {
        return run;
    }

    public @Nonnull
    String getDisplayName() {
        return displayName;
    }

    private static NodeType getNodeType(FlowNode node) {
        if (PipelineNodeUtil.isStage(node)) {
            return NodeType.STAGE;
        } else if (PipelineNodeUtil.isParallelBranch(node)) {
            return NodeType.PARALLEL;
        } else if (node instanceof AtomNode) {
            return NodeType.STEP;
        }
        throw new IllegalArgumentException(String.format("Unknown FlowNode %s, type: %s", node.getId(), node.getClass()));
    }

    public @Nonnull
    NodeRunStatus getStatus() {
        if (hasBlockError()) {
            if (isBlockErrorInterruptedWithAbort()) {
                return new NodeRunStatus(BlueRunResult.ABORTED, BlueRunState.FINISHED);
            } else {
                return new NodeRunStatus(BlueRunResult.FAILURE, BlueRunState.FINISHED);
            }
        }
        return status;
    }

    public @Nonnull
    TimingInfo getTiming() {
        return timingInfo;
    }

    public @Nonnull
    String getId() {
        return node.getId();
    }

    public @Nonnull
    FlowNode getNode() {
        return node;
    }

    public NodeType getType() {
        return type;
    }

    public void addEdge(FlowNodeWrapper edge) {
        this.edges.add(edge);
    }

    public void addEdges(List<FlowNodeWrapper> edges) {
        this.edges.addAll(edges);
    }

    public void addParent(FlowNodeWrapper parent) {
        parents.add(parent);
    }

    public void addParents(Collection<FlowNodeWrapper> parents) {
        this.parents.addAll(parents);
    }

    public @CheckForNull
    FlowNodeWrapper getFirstParent() {
        return parents.size() > 0 ? parents.get(0) : null;
    }

    public @Nonnull
    List<FlowNodeWrapper> getParents() {
        return parents;
    }

    public String getCauseOfFailure() {
        return causeOfFailure;
    }

    public void setCauseOfFailure(String causeOfFailure) {
        this.causeOfFailure = causeOfFailure;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FlowNodeWrapper)) {
            return false;
        }
        return node.equals(((FlowNodeWrapper) obj).node);
    }

    public @CheckForNull
    InputStep getInputStep() {
        return inputStep;
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getName() + "[id=" + node.getId() + ",displayName=" + this.displayName + ",type=" + this.type + "]";
    }

    boolean hasBlockError() {
        return blockErrorAction != null
            && blockErrorAction.getError() != null;
    }

    String blockError() {
        if (hasBlockError()) {
            return blockErrorAction.getError().getMessage();
        }
        return null;
    }

    @CheckForNull
    String nodeError() {
        ErrorAction errorAction = node.getError();
        if (errorAction != null) {
            return errorAction.getError().getMessage();
        }
        return null;
    }

    boolean isBlockErrorInterruptedWithAbort() {
        if (hasBlockError()) {
            Throwable error = blockErrorAction.getError();
            if (error instanceof FlowInterruptedException) {
                FlowInterruptedException interrupted = (FlowInterruptedException) error;
                return interrupted.getResult().equals(Result.ABORTED);
            }
        }
        return false;
    }

    boolean isLoggable() {
        return PipelineNodeUtil.isLoggable.apply(node);
    }

    public void setBlockErrorAction(ErrorAction blockErrorAction) {
        this.blockErrorAction = blockErrorAction;
    }

    /**
     * Returns Action instances that were attached to the associated FlowNode, or to any of its children
     * not represented in the graph.
     * Filters by class to mimic Item.getActions(class).
     */
    public <T extends Action> Collection<T> getPipelineActions(Class<T> clazz) {
        if (pipelineActions == null) {
            return Collections.emptyList();
        }
        ArrayList<T> filtered = new ArrayList<>();
        for (Action a : pipelineActions) {
            if (clazz.isInstance(a)) {
                filtered.add(clazz.cast(a));
            }
        }
        return filtered;
    }

    /**
     * Returns Action instances that were attached to the associated FlowNode, or to any of its children
     * not represented in the graph.
     */
    public Collection<Action> getPipelineActions() {
        return Collections.unmodifiableCollection(this.pipelineActions);
    }

    public void setPipelineActions(Collection<Action> pipelineActions) {
        this.pipelineActions = pipelineActions;
    }
}
