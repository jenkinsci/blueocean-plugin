package io.jenkins.blueocean.rest.impl.pipeline;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Action;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.actions.ExecutionModelAction;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.BlockEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graph.FlowStartNode;
import org.jenkinsci.plugins.workflow.graphanalysis.DepthFirstScanner;
import org.jenkinsci.plugins.workflow.graphanalysis.ForkScanner;
import org.jenkinsci.plugins.workflow.graphanalysis.MemoryFlowChunk;
import org.jenkinsci.plugins.workflow.graphanalysis.StandardChunkVisitor;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.GenericStatus;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.StageChunkFinder;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.StatusAndTiming;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.TimingInfo;
import org.jenkinsci.plugins.workflow.support.actions.PauseAction;
import org.jenkinsci.plugins.workflow.support.steps.input.InputAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * @author Vivek Pandey
 * <p>
 * Run your Jenkins instance with <code>-DNODE-DUMP-ENABLED</code> to turn on the logging when diagnosing bugs! You'll
 * also need to have a logging config that enables debug for (at least) this class. Use
 * <code>-Djava.util.logging.config.file=./logging.properties</code> to set a custom logging properties file from the
 * command line, or do it from within the admin UI.
 */
public class PipelineNodeGraphVisitor extends StandardChunkVisitor implements NodeGraphBuilder {
    private final WorkflowRun run;

    private final ArrayDeque<FlowNodeWrapper> parallelBranches = new ArrayDeque<>();

    public final ArrayDeque<FlowNodeWrapper> nodes = new ArrayDeque<>();

    private FlowNode firstExecuted = null;

    private FlowNodeWrapper nextStage;

    private Stack<FlowNode> parallelEnds = new Stack<>();

    public final Map<String, FlowNodeWrapper> nodeMap = new LinkedHashMap<>();

    public final Map<String, Stack<FlowNodeWrapper>> stackPerEnd = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(PipelineNodeGraphVisitor.class);

    private final boolean isNodeVisitorDumpEnabled = Boolean.getBoolean("NODE-DUMP-ENABLED") && logger.isDebugEnabled();

    private final Stack<FlowNode> nestedStages = new Stack<>();
    private final Stack<FlowNode> nestedbranches = new Stack<>();

    private final ArrayDeque<FlowNode> pendingInputSteps = new ArrayDeque<>();

    private final Stack<FlowNode> pendingBranchEndNodes = new Stack<>();
    private final Stack<FlowNode> parallelBranchEndNodes = new Stack<>();
    private final Stack<FlowNode> parallelBranchStartNodes = new Stack<>();

    private final InputAction inputAction;

    private StepStartNode agentNode = null;

    // Collects instances of Action as we walk up the graph, to be drained when appropriate
    private Set<Action> pipelineActions;

    // Temporary holding for actions waiting to be assigned to the wrapper for a branch
    private Map<FlowNode /* is the branchStartNode */, Set<Action>> pendingActionsForBranches;

    private final static String PARALLEL_SYNTHETIC_STAGE_NAME = "Parallel";

    private final boolean declarative;

    public PipelineNodeGraphVisitor(WorkflowRun run) {
        this.run = run;
        this.inputAction = run.getAction(InputAction.class);
        this.pipelineActions = new HashSet<>();
        this.pendingActionsForBranches = new HashMap<>();
        declarative = run.getAction(ExecutionModelAction.class) != null;
        FlowExecution execution = run.getExecution();
        if (execution != null) {
            try {
                ForkScanner.visitSimpleChunks(execution.getCurrentHeads(), this, new StageChunkFinder());
            } catch (final Throwable t) {
                // Log run ID, because the eventual exception handler (probably Stapler) isn't specific enough to do so
                logger.error("Caught a " + t.getClass().getSimpleName() +
                                 " traversing the graph for run " + run.getExternalizableId());
                throw t;
            }
        } else {
            logger.debug("Could not find execution for run " + run.getExternalizableId());
        }
    }

    @Override
    public void chunkStart(@Nonnull FlowNode startNode, @CheckForNull FlowNode beforeBlock, @Nonnull ForkScanner scanner) {
        super.chunkStart(startNode, beforeBlock, scanner);
        if (isNodeVisitorDumpEnabled) {
            dump(String.format("chunkStart=> id: %s, name: %s, function: %s", startNode.getId(),
                               startNode.getDisplayName(), startNode.getDisplayFunctionName()));
        }

        if (PipelineNodeUtil.isSyntheticStage(startNode)) {
            return;
        }
        if (NotExecutedNodeAction.isExecuted(startNode)) {
            firstExecuted = startNode;
        }
    }

    @Override
    public void chunkEnd(@Nonnull FlowNode endNode, @CheckForNull FlowNode afterBlock, @Nonnull ForkScanner scanner) {
        super.chunkEnd(endNode, afterBlock, scanner);

        if (isNodeVisitorDumpEnabled) {
            dump(String.format("chunkEnd=> id: %s, name: %s, function: %s, type:%s", endNode.getId(),
                               endNode.getDisplayName(), endNode.getDisplayFunctionName(), endNode.getClass()));
        }

        if (isNodeVisitorDumpEnabled && endNode instanceof StepEndNode) {
            dump("\tStartNode: " + ((StepEndNode) endNode).getStartNode());
        }

        if (endNode instanceof StepStartNode && PipelineNodeUtil.isAgentStart(endNode)) {
            agentNode = (StepStartNode) endNode;
        }

        // capture orphan branches
        captureOrphanParallelBranches();

        //if block stage node push it to stack as it may have nested stages
        if (parallelEnds.isEmpty() &&
            endNode instanceof StepEndNode
            && !PipelineNodeUtil.isSyntheticStage(((StepEndNode) endNode).getStartNode()) //skip synthetic stages
            && PipelineNodeUtil.isStage(((StepEndNode) endNode).getStartNode())) {

            //XXX: There seems to be bug in eventing, chunkEnd is sent twice for the same FlowNode
            //     Lets peek and if the last one is same as this endNode then skip adding it
            FlowNode node = null;
            if (!nestedStages.empty()) {
                node = nestedStages.peek();
            }
            if (node == null || !node.equals(endNode)) {
                nestedStages.push(endNode);
            }

        }
        firstExecuted = null;

        // if we're using marker-based (and not block-scoped) stages, add the last node as part of its contents
        if (!(endNode instanceof BlockEndNode)) {
            atomNode(null, endNode, afterBlock, scanner);
        }
    }

    // This gets triggered on encountering a new chunk (stage or branch)
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "chunk.getLastNode() is marked non null but is null sometimes, when JENKINS-40200 is fixed we will remove this check ")
    @Override
    protected void handleChunkDone(@Nonnull MemoryFlowChunk chunk) {

        if (isNodeVisitorDumpEnabled) {
            dump(String.format("handleChunkDone=> id: %s, name: %s, function: %s", chunk.getFirstNode().getId(),
                               chunk.getFirstNode().getDisplayName(), chunk.getFirstNode().getDisplayFunctionName()));
        }

        if (PipelineNodeUtil.isSyntheticStage(chunk.getFirstNode())) {
            return;
        }

        boolean parallelNestedStages = false;

        // it's nested stages inside parallel so let's collect them later
        if (!parallelEnds.isEmpty()) {
            parallelNestedStages = true;
        }

        if (!nestedStages.empty()) {
            FlowNode discarded = nestedStages.pop(); //we throw away first nested stage
            // nested stages not supported in scripted pipeline.
            if (!nestedStages.isEmpty() && !isDeclarative()) {
                return;
            }
        }

        TimingInfo times = null;

        //TODO: remove chunk.getLastNode() != null check based on how JENKINS-40200 gets resolved
        if (firstExecuted != null && chunk.getLastNode() != null) {
            times = StatusAndTiming.computeChunkTiming(run, chunk.getPauseTimeMillis(), firstExecuted, chunk.getLastNode(), chunk.getNodeAfter());
        }

        if (times == null) {
            times = new TimingInfo();
        }

        NodeRunStatus status;
        boolean skippedStage = PipelineNodeUtil.isSkippedStage(chunk.getFirstNode());
        if (skippedStage) {
            status = new NodeRunStatus(BlueRun.BlueRunResult.NOT_BUILT, BlueRun.BlueRunState.SKIPPED);
        } else if (firstExecuted == null) {
            status = new NodeRunStatus(GenericStatus.NOT_EXECUTED);
        } else if (chunk.getLastNode() != null) {
            // StatusAndTiming.computeChunkStatus2 seems to return wrong status for parallel sequential stages
            // so check check if active and in the case of nested sequential
            if (parallelNestedStages && chunk.getFirstNode().isActive()) {
                status = new NodeRunStatus(chunk.getFirstNode());
            } else {
                FlowNode nodeBefore = chunk.getNodeBefore(), firstNode = chunk.getFirstNode(), lastNode = chunk.getLastNode(), nodeAfter = chunk.getNodeAfter();
                status = new NodeRunStatus(
                    StatusAndTiming.computeChunkStatus2(run, nodeBefore, firstNode, lastNode, nodeAfter));
            }
        } else {
            status = new NodeRunStatus(firstExecuted);
        }

        if (!pendingInputSteps.isEmpty()) {
            status = new NodeRunStatus(BlueRun.BlueRunResult.UNKNOWN, BlueRun.BlueRunState.PAUSED);
        }
        FlowNodeWrapper stage = new FlowNodeWrapper(chunk.getFirstNode(), status, times, run);

        stage.setCauseOfFailure(PipelineNodeUtil.getCauseOfBlockage(stage.getNode(), agentNode));

        accumulatePipelineActions(chunk.getFirstNode());
        final Set<Action> pipelineActions = drainPipelineActions();
        if (isNodeVisitorDumpEnabled && pipelineActions.size() > 0) {
            dump("\tAdding " + pipelineActions.size() + " actions to stage id: " + stage.getId());
        }
        stage.setPipelineActions(pipelineActions);

        if (!parallelNestedStages) {
            nodes.push(stage);
            nodeMap.put(stage.getId(), stage);
        }
        if (!skippedStage && !parallelBranches.isEmpty()) {
            Iterator<FlowNodeWrapper> branches = parallelBranches.descendingIterator();
            while (branches.hasNext()) {
                FlowNodeWrapper p = branches.next();
                p.addParent(stage);
                stage.addEdge(p);
            }
        } else {
            if (parallelNestedStages) {
                if (pendingBranchEndNodes.isEmpty()) {
                    logger.debug("skip parsing stage {} but parallelBranchEndNodes is empty", stage);
                } else {
                    String endId = pendingBranchEndNodes.peek().getId();
                    Stack<FlowNodeWrapper> stack = stackPerEnd.get(endId);
                    if (stack == null) {
                        stack = new Stack<>();
                        stackPerEnd.put(endId, stack);
                    }
                    stack.add(stage);
                }
            }
            if (nextStage != null && !parallelNestedStages) {
                nextStage.addParent(stage);
                stage.addEdge(nextStage);
            }
        }
        parallelBranches.clear();
        if (!parallelNestedStages) {
            this.nextStage = stage;
        }
    }


    @Override
    protected void resetChunk(@Nonnull MemoryFlowChunk chunk) {
        super.resetChunk(chunk);
        firstExecuted = null;
        pendingInputSteps.clear();
    }

    @Override
    public void parallelStart(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchNode, @Nonnull ForkScanner scanner) {
        if (isNodeVisitorDumpEnabled) {
            dump(String.format("parallelStart=> id: %s, name: %s, function: %s", parallelStartNode.getId(),
                               parallelStartNode.getDisplayName(), parallelStartNode.getDisplayFunctionName()));
            dump(String.format("\tbranch=> id: %s, name: %s, function: %s", branchNode.getId(),
                               branchNode.getDisplayName(), branchNode.getDisplayFunctionName()));
        }

        if (nestedbranches.size() != parallelBranchEndNodes.size()) {
            logger.debug(String.format("nestedBranches size: %s not equal to parallelBranchEndNodes: %s",
                                       nestedbranches.size(), parallelBranchEndNodes.size()));
            if (!parallelEnds.isEmpty()) {
                parallelEnds.pop();
            }
            return;
        }

        if (!pendingBranchEndNodes.isEmpty()) {
            logger.debug("Found parallelBranchEndNodes, but the corresponding branchStartNode yet");
            if (!parallelEnds.isEmpty()) {
                parallelEnds.pop();
            }
            return;
        }

        while (!nestedbranches.empty() && !parallelBranchEndNodes.empty()) {
            FlowNode branchStartNode = nestedbranches.pop();
            FlowNode endNode = parallelBranchEndNodes.pop();

            if (isNodeVisitorDumpEnabled) {
                dump(String.format("\tBranch with start node id: %s", branchStartNode.getId()));
            }

            TimingInfo times;
            NodeRunStatus status;

            if (endNode != null) {
                // Branch has completed
                times = StatusAndTiming.computeChunkTiming(run, chunk.getPauseTimeMillis(), branchStartNode, endNode,
                                                           chunk.getNodeAfter());
                if (endNode instanceof StepAtomNode) {
                    if (PipelineNodeUtil.isPausedForInputStep((StepAtomNode) endNode, inputAction)) {
                        status = new NodeRunStatus(BlueRun.BlueRunResult.UNKNOWN, BlueRun.BlueRunState.PAUSED);
                    } else {
                        status = new NodeRunStatus(endNode);
                    }
                } else {
                    FlowNode parallelEnd = null;
                    if (!parallelEnds.isEmpty()) {
                        parallelEnd = parallelEnds.peek();
                    }
                    GenericStatus genericStatus =
                        StatusAndTiming.computeChunkStatus2(run, parallelStartNode, branchStartNode, endNode, parallelEnd);
                    status = new NodeRunStatus(genericStatus);
                }
            } else {
                // Branch still running / paused
                long startTime = System.currentTimeMillis();
                if (branchStartNode.getAction(TimingAction.class) != null) {
                    startTime = TimingAction.getStartTime(branchStartNode);
                }
                times = new TimingInfo(System.currentTimeMillis() - startTime,
                                       chunk.getPauseTimeMillis(),
                                       startTime);
                status = new NodeRunStatus(BlueRun.BlueRunResult.UNKNOWN, BlueRun.BlueRunState.RUNNING);
            }
            assert times != null; //keep FB happy

            FlowNodeWrapper branch = new FlowNodeWrapper(branchStartNode, status, times, run);

            // Collect any pending actions (required for most-recently-handled branch)
            ArrayList<Action> branchActions = new ArrayList<>(drainPipelineActions());

            // Add actions for this branch, which are collected when changing branches
            if (pendingActionsForBranches.containsKey(branchStartNode)) {
                branchActions.addAll(pendingActionsForBranches.get(branchStartNode));
                pendingActionsForBranches.remove(branchStartNode);
            }

            if (isNodeVisitorDumpEnabled && branchActions.size() > 0) {
                dump("\t\tAdding " + branchActions.size() + " actions to branch id: " + branch.getId());
            }

            // Check the stack for this branch, for declarative pipelines it should be non-empty even if only 1 stage
            Stack<FlowNodeWrapper> stack = stackPerEnd.get(endNode.getId());
            if (stack != null && !stack.isEmpty()) {

                if (isNodeVisitorDumpEnabled) {
                    dump(String.format("\t\t\"Complex\" stages detected (%d)", stack.size()));
                }

                if (stack.size() == 1) {
                    FlowNodeWrapper firstNodeWrapper = stack.pop();
                    // We've got a single non-nested stage for this branch. We're going to discard it from our graph,
                    // since we use the node for the branch itself, which has been created with the same name...

                    if (isNodeVisitorDumpEnabled) {
                        dump("\t\tSingle-stage branch");
                    }

                    // ...but first we need to poach any actions from the stage node we'll be discarding
                    branchActions.addAll(firstNodeWrapper.getPipelineActions());

                    if (nextStage != null) {
                        branch.addEdge(nextStage);
                    }
                } else {
                    if (isDeclarative()) {
                        // Declarative parallel pipeline scenario
                        // We've got nested stages for sequential stage branches and/or branch labelling purposes
                        FlowNodeWrapper firstNodeWrapper = stack.pop();

                        // Usually ignore firstNodeWrapper, but if the first stage has a different name...
                        if (!StringUtils.equals(firstNodeWrapper.getDisplayName(), branch.getDisplayName())) {
                            // we record this node so the UI can show the label for the branch...
                            if (isNodeVisitorDumpEnabled) {
                                dump("\t\tNested labelling stage detected");
                            }
                            branch.addEdge(firstNodeWrapper);
                            firstNodeWrapper.addParent(branch);
                            nodes.add(firstNodeWrapper);
                            // Note that there's no edge from this labelling node to the rest of the branch stages
                        }
                    }
                    // Declarative and scripted parallel pipeline scenario
                    FlowNodeWrapper previousNode = branch;
                    while (!stack.isEmpty()) {
                        // Grab next, link to prev, add to result
                        FlowNodeWrapper currentStage = stack.pop();
                        previousNode.addEdge(currentStage);
                        currentStage.addParent(previousNode);
                        nodes.add(currentStage);
                        previousNode = currentStage;
                    }

                    if (nextStage != null) {
                        previousNode.addEdge(nextStage);
                    }
                }
            } else {
                // Classic pipeline branch, single stage

                if (isNodeVisitorDumpEnabled) {
                    dump("\t\tSimple branch (classic pipeline)");
                }

                if (nextStage != null) {
                    branch.addEdge(nextStage);
                }
            }

            branch.setPipelineActions(branchActions);
            parallelBranches.push(branch);
        }

        FlowNodeWrapper[] sortedBranches = parallelBranches.toArray(new FlowNodeWrapper[parallelBranches.size()]);
        Arrays.sort(sortedBranches, Comparator.comparing(FlowNodeWrapper::getDisplayName));

        parallelBranches.clear();
        for (int i = 0; i < sortedBranches.length; i++) {
            parallelBranches.push(sortedBranches[i]);
        }
        for (FlowNodeWrapper p : parallelBranches) {
            nodes.push(p);
            nodeMap.put(p.getId(), p);
        }

        //Removes parallelEnd node for next parallel block
        if (!parallelEnds.isEmpty()) {
            parallelEnds.pop();
        }
    }

    @Override
    public void parallelEnd(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode parallelEndNode, @Nonnull ForkScanner scanner) {
        if (isNodeVisitorDumpEnabled) {
            dump(String.format("parallelEnd=> id: %s, name: %s, function: %s", parallelEndNode.getId(),
                               parallelEndNode.getDisplayName(), parallelEndNode.getDisplayFunctionName()));
            if (parallelEndNode instanceof StepEndNode) {
                dump(String.format("parallelEnd=> id: %s, StartNode: %s, name: %s, function: %s", parallelEndNode.getId(),
                                   ((StepEndNode) parallelEndNode).getStartNode().getId(), ((StepEndNode) parallelEndNode).getStartNode().getDisplayName(), ((StepEndNode) parallelEndNode).getStartNode().getDisplayFunctionName()));
            }
        }
        captureOrphanParallelBranches();
        this.parallelEnds.add(parallelEndNode);
    }

    @Override
    public void parallelBranchStart(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchStartNode, @Nonnull ForkScanner scanner) {
        if (isNodeVisitorDumpEnabled) {
            dump(String.format("parallelBranchStart=> id: %s, name: %s, function: %s", branchStartNode.getId(),
                               branchStartNode.getDisplayName(), branchStartNode.getDisplayFunctionName()));
        }

        // Save actions for this branch, so we can add them to the FlowNodeWrapper later
        pendingActionsForBranches.put(branchStartNode, drainPipelineActions());
        nestedbranches.push(branchStartNode);
        if (!pendingBranchEndNodes.isEmpty()) {
            FlowNode endNode = pendingBranchEndNodes.pop();
            parallelBranchEndNodes.add(endNode);
        }
    }

    @Override
    public void parallelBranchEnd(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchEndNode, @Nonnull ForkScanner scanner) {
        if (isNodeVisitorDumpEnabled) {
            dump(String.format("parallelBranchEnd=> id: %s, name: %s, function: %s, type: %s", branchEndNode.getId(),
                               branchEndNode.getDisplayName(), branchEndNode.getDisplayFunctionName(), branchEndNode.getClass()));
            if (branchEndNode instanceof StepEndNode) {
                dump(String.format("parallelBranchEnd=> id: %s, StartNode: %s, name: %s, function: %s", branchEndNode.getId(),
                                   ((StepEndNode) branchEndNode).getStartNode().getId(), ((StepEndNode) branchEndNode).getStartNode().getDisplayName(),
                                   ((StepEndNode) branchEndNode).getStartNode().getDisplayFunctionName()));
            }
        }
        pendingBranchEndNodes.add(branchEndNode);
        parallelBranchStartNodes.add(parallelStartNode);
    }

    @Override
    public void atomNode(@CheckForNull FlowNode before, @Nonnull FlowNode atomNode,
                         @CheckForNull FlowNode after, @Nonnull ForkScanner scan) {
        if (isNodeVisitorDumpEnabled) {
            dump(String.format("atomNode=> id: %s, name: %s, function: %s, type: %s", atomNode.getId(),
                               atomNode.getDisplayName(), atomNode.getDisplayFunctionName(), atomNode.getClass()));
        }

        accumulatePipelineActions(atomNode);

        if (atomNode instanceof FlowStartNode) {
            captureOrphanParallelBranches();
            return;
        }

        if (NotExecutedNodeAction.isExecuted(atomNode)) {
            firstExecuted = atomNode;
        }
        long pause = PauseAction.getPauseDuration(atomNode);
        chunk.setPauseTimeMillis(chunk.getPauseTimeMillis() + pause);

        if (atomNode instanceof StepAtomNode
            && PipelineNodeUtil.isPausedForInputStep((StepAtomNode) atomNode, inputAction)) {
            pendingInputSteps.add(atomNode);
        }
    }

    private void dump(String str) {
        logger.debug(System.identityHashCode(this) + ": " + str);
    }

    /**
     * Find any Actions on this node, and add them to the pipelineActions collection until we can attach
     * them to a FlowNodeWrapper.
     */
    protected void accumulatePipelineActions(FlowNode node) {
        final List<Action> actions = node.getActions(Action.class);
        pipelineActions.addAll(actions);
        if (isNodeVisitorDumpEnabled) {
            dump(String.format("\t\taccumulating actions - added %d, total is %d", actions.size(), pipelineActions.size()));
        }
    }

    /**
     * Empty the pipelineActions buffer, returning its contents.
     */
    protected Set<Action> drainPipelineActions() {
        if (isNodeVisitorDumpEnabled) {
            dump(String.format("\t\tdraining accumulated actions - total is %d", pipelineActions.size()));
        }

        if (pipelineActions.size() == 0) {
            return Collections.emptySet();
        }

        Set<Action> drainedActions = pipelineActions;
        pipelineActions = new HashSet<>();
        return drainedActions;
    }

    @Override
    public List<FlowNodeWrapper> getPipelineNodes() {
        return new ArrayList<>(nodes);
    }

    @Override
    public List<BluePipelineNode> getPipelineNodes(final Link parent) {
        return this.nodes.stream()
                         .map(n -> new PipelineNodeImpl(n, () -> parent, run))
                         .collect(Collectors.toList());
    }

    @Override
    public List<BluePipelineStep> getPipelineNodeSteps(final String nodeId, Link parent) {
        FlowExecution execution = run.getExecution();
        if (execution == null) {
            logger.debug(String.format("Pipeline %s, runid %s  has null execution", run.getParent().getName(), run.getId()));
            return Collections.emptyList();
        }
        DepthFirstScanner depthFirstScanner = new DepthFirstScanner();
        //If blocked scope, get the end node
        FlowNode n = depthFirstScanner
            .findFirstMatch(execution.getCurrentHeads(),
                            input -> (input != null
                                && input.getId().equals(nodeId)
                                && (PipelineNodeUtil.isStage(input) || PipelineNodeUtil.isParallelBranch(input))));

        if (n == null) { //if no node found or the node is not stage or parallel we return empty steps
            return Collections.emptyList();
        }
        PipelineStepVisitor visitor = new PipelineStepVisitor(run, n);
        ForkScanner.visitSimpleChunks(execution.getCurrentHeads(), visitor, new StageChunkFinder());
        return visitor.getSteps()
                      .stream()
                      .map(node -> new PipelineStepImpl(node, parent))
                      .collect(Collectors.toList());
    }

    @Override
    public List<BluePipelineStep> getPipelineNodeSteps(Link parent) {
        FlowExecution execution = run.getExecution();
        if (execution == null) {
            return Collections.emptyList();
        }
        PipelineStepVisitor visitor = new PipelineStepVisitor(run, null);
        ForkScanner.visitSimpleChunks(execution.getCurrentHeads(), visitor, new StageChunkFinder());
        return visitor.getSteps()
                      .stream()
                      .map(node -> new PipelineStepImpl(node, parent))
                      .collect(Collectors.toList());
    }

    @Override
    public BluePipelineStep getPipelineNodeStep(String id, Link parent) {
        FlowExecution execution = run.getExecution();
        if (execution == null) {
            return null;
        }
        PipelineStepVisitor visitor = new PipelineStepVisitor(run, null);
        ForkScanner.visitSimpleChunks(execution.getCurrentHeads(), visitor, new StageChunkFinder());
        FlowNodeWrapper node = visitor.getStep(id);
        if (node == null) {
            return null;
        }
        return new PipelineStepImpl(node, parent);
    }

    private static Optional<FlowNodeWrapper> findNodeWrapperByIdIn(String id, Collection<FlowNodeWrapper> nodes) {
        for (FlowNodeWrapper node : nodes) {
            if (node.getId().equals(id)) {
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    /**
     * Instead of looking up by id, sometimes the IDs are wrong during build, so we look based on the parentage
     * and display name.
     */
    private static Optional<FlowNodeWrapper> findNodeWrapperByParentageIn(FlowNodeWrapper example, List<FlowNodeWrapper> nodes) {
        final String exampleDisplayName = example.getDisplayName();
        final FlowNodeWrapper firstParent = example.getFirstParent();
        String exampleParentName = firstParent == null ? "" : firstParent.getDisplayName();

        for (FlowNodeWrapper node : nodes) {
            FlowNodeWrapper nodeParent = node.getFirstParent();
            String nodeParentName = nodeParent == null ? "": nodeParent.getDisplayName();
            if (node.getDisplayName().equals(exampleDisplayName) && nodeParentName.equals(exampleParentName)) {
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<BluePipelineNode> union(List<FlowNodeWrapper> previousNodes, final Link parent) {

        boolean graphsAreCompatible = true;

        // A map from the ID of a node from previousNodes to the corresponding node for the current build.
        Map<String, FlowNodeWrapper> newNodes = new HashMap<>(previousNodes.size());

        // Start with the currently-executing nodes
        for (FlowNodeWrapper currentNodeWrapper : nodes) {
            final String nodeId = currentNodeWrapper.getId();
            Optional<FlowNodeWrapper> maybeOldNode = findNodeWrapperByIdIn(nodeId, previousNodes);

            // If we can't find the same node by ID, we will search via name + firstparent
            if (!maybeOldNode.isPresent()) {
                maybeOldNode = findNodeWrapperByParentageIn(currentNodeWrapper, previousNodes);
            }

            if (maybeOldNode.isPresent()) {
                FlowNodeWrapper oldNodeWrapper = maybeOldNode.get();

                if (!currentNodeWrapper.probablySameNode(oldNodeWrapper)) {
                    // Graphs not the same, user probably changed pipleine definition
                    graphsAreCompatible = false;
                    break;
                }

                // New wrapper object based on current execution
                FlowNodeWrapper newNodeWrapper = new FlowNodeWrapper(
                    currentNodeWrapper.getNode(),
                    currentNodeWrapper.getStatus(),
                    currentNodeWrapper.getTiming(),
                    currentNodeWrapper.getInputStep(),
                    run
                );

                newNodes.put(oldNodeWrapper.getId(), newNodeWrapper);

            } else {
                // Node does not exist in previous graph, user probably changed pipleine definition.
                graphsAreCompatible = false;
                break;
            }
        }

        // Walk the old graph, create new wrappers for any stages not yet started
        if (graphsAreCompatible) {
            for (FlowNodeWrapper oldNodeWrapper : previousNodes) {
                final String oldNodeId = oldNodeWrapper.getId();

                if (!newNodes.containsKey(oldNodeId)) {
                    FlowNodeWrapper newNodeWrapper = new FlowNodeWrapper(
                        oldNodeWrapper.getNode(),
                        new NodeRunStatus(null, null),
                        new TimingInfo(),
                        null,
                        run
                    );

                    newNodes.put(oldNodeId, newNodeWrapper);
                }
            }
        }

        // Re-create edges and parentage based on previous run
        if (graphsAreCompatible) {
            for (FlowNodeWrapper oldNodeWrapper : previousNodes) {
                final String oldNodeId = oldNodeWrapper.getId();

                FlowNodeWrapper newNodeWrapper = newNodes.get(oldNodeId);

                for (FlowNodeWrapper oldEdge : oldNodeWrapper.edges) {
                    FlowNodeWrapper newEdge = newNodes.get(oldEdge.getId());
                    newNodeWrapper.addEdge(newEdge);
                }

                for (FlowNodeWrapper oldParent: oldNodeWrapper.getParents()) {
                    FlowNodeWrapper newParent = newNodes.get(oldParent.getId());
                    newNodeWrapper.addParent(newParent);
                }
            }
        }

        // Convert the set of FlowNodeWrapper objs into a list of BluePipelineNode objs, and return it.
        if (graphsAreCompatible) {

            ArrayList<BluePipelineNode> newList = new ArrayList<>(newNodes.size());

            // We'll add nodes to the list in the original order, to reduce risk to downstream
            // code that makes too many assumptions

            for (FlowNodeWrapper oldNodeWrapper : previousNodes) {
                final String oldNodeId = oldNodeWrapper.getId();
                FlowNodeWrapper newNodeWrapper = newNodes.get(oldNodeId);
                newList.add(new PipelineNodeImpl(newNodeWrapper, () -> parent, run));
            }

            return newList;
        }

        // Fallback for when the graphs cannot be merged
        return this.getPipelineNodes(parent);
    }

    private void captureOrphanParallelBranches() {
        if (!parallelBranches.isEmpty() && (firstExecuted == null
            || !PipelineNodeUtil.isStage(firstExecuted)
        )) {
            FlowNodeWrapper synStage = createParallelSyntheticNode();
            if (synStage != null) {
                nodes.push(synStage);
                nodeMap.put(synStage.getId(), synStage);
                parallelBranches.clear();
                this.nextStage = synStage;
            }
        }
    }

    /**
     * Create synthetic stage that wraps a parallel block at top level, that is not enclosed inside a stage.
     */
    private @Nullable
    FlowNodeWrapper createParallelSyntheticNode() {

        if (parallelBranches.isEmpty()) {
            return null;
        }

        FlowNodeWrapper firstBranch = parallelBranches.getLast();
        FlowNodeWrapper parallel = firstBranch.getFirstParent();

        if (isNodeVisitorDumpEnabled) {
            dump(String.format("createParallelSyntheticNode=> firstBranch: %s, parallel:%s", firstBranch.getId(), (parallel == null ? "(none)" : parallel.getId())));
        }

        String firstNodeId = firstBranch.getId();
        List<FlowNode> parents;
        if (parallel != null) {
            parents = parallel.getNode().getParents();
        } else {
            parents = new ArrayList<>();
        }
        FlowNode syntheticNode = new FlowNode(firstBranch.getNode().getExecution(),
                                              createSyntheticStageId(firstNodeId, PARALLEL_SYNTHETIC_STAGE_NAME), parents) {
            @Override
            public void save() throws IOException {
                // no-op to avoid JENKINS-45892 violations from serializing the synthetic FlowNode.
            }

            @Override
            protected String getTypeDisplayName() {
                return PARALLEL_SYNTHETIC_STAGE_NAME;
            }
        };

        syntheticNode.addAction(new LabelAction(PARALLEL_SYNTHETIC_STAGE_NAME));

        long duration = 0;
        long pauseDuration = 0;
        long startTime = System.currentTimeMillis();
        boolean isCompleted = true;
        boolean isPaused = false;
        boolean isFailure = false;
        boolean isUnknown = false;
        for (FlowNodeWrapper pb : parallelBranches) {
            if (!isPaused && pb.getStatus().getState() == BlueRun.BlueRunState.PAUSED) {
                isPaused = true;
            }
            if (isCompleted && pb.getStatus().getState() != BlueRun.BlueRunState.FINISHED) {
                isCompleted = false;
            }

            if (!isFailure && pb.getStatus().getResult() == BlueRun.BlueRunResult.FAILURE) {
                isFailure = true;
            }
            if (!isUnknown && pb.getStatus().getResult() == BlueRun.BlueRunResult.UNKNOWN) {
                isUnknown = true;
            }
            duration += pb.getTiming().getTotalDurationMillis();
            pauseDuration += pb.getTiming().getPauseDurationMillis();
        }

        BlueRun.BlueRunState state = isCompleted ? BlueRun.BlueRunState.FINISHED :
                                     (isPaused ? BlueRun.BlueRunState.PAUSED : BlueRun.BlueRunState.RUNNING);
        BlueRun.BlueRunResult result = isFailure ? BlueRun.BlueRunResult.FAILURE :
                                       (isUnknown ? BlueRun.BlueRunResult.UNKNOWN : BlueRun.BlueRunResult.SUCCESS);

        TimingInfo timingInfo = new TimingInfo(duration, pauseDuration, startTime);

        FlowNodeWrapper synStage = new FlowNodeWrapper(syntheticNode, new NodeRunStatus(result, state), timingInfo, run);

        Iterator<FlowNodeWrapper> sortedBranches = parallelBranches.descendingIterator();
        while (sortedBranches.hasNext()) {
            FlowNodeWrapper p = sortedBranches.next();
            p.addParent(synStage);
            synStage.addEdge(p);
        }
        return synStage;
    }

    public boolean isDeclarative() {
        return declarative;
    }

    /**
     * Create id of synthetic stage in a deterministic base.
     * <p>
     * For example, an orphan parallel block with id 12 (appears top level not wrapped inside a stage) gets wrapped in a synthetic
     * stage with id: 12-parallel-synthetic. Later client calls nodes API using this id: /nodes/12-parallel-synthetic/ would
     * correctly pick the synthetic stage wrapping parallel block 12 by doing a lookup nodeMap.get("12-parallel-synthetic")
     */
    private @Nonnull
    String createSyntheticStageId(@Nonnull String firstNodeId, @Nonnull String syntheticStageName) {
        return String.format("%s-%s-synthetic", firstNodeId, syntheticStageName.toLowerCase());
    }
}
