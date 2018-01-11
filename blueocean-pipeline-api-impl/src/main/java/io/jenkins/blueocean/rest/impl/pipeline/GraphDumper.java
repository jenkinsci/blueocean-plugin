package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.service.embedded.rest.NodeDownstreamBuildAction;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.BlockEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graphanalysis.ForkScanner;
import org.jenkinsci.plugins.workflow.graphanalysis.SimpleChunkVisitor;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.StageChunkFinder;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;


// TODO: Document this and/or pull it and stash it somewhere for next time I need it.

// Dump the following into PipelineNodeContainerImpl:
//
//        if (Boolean.getBoolean("DUMP-DOT")) {
//            GraphDumper gd = new GraphDumper(run);
//            for (FlowNodeWrapper node : graphBuilder.getPipelineNodes()) {
//                gd.flagNode(node.getId());
//            }
//            System.out.println("\n\n\n\n" + gd.getDotGraph() + "\n\n\n\n");
//        }

public class GraphDumper implements SimpleChunkVisitor {

    private final HashMap<String /* node id */, NodeInfo> graph;
    private final HashMap<String /* node id */, FlowNode> allNodes;
    private final WorkflowRun run;

    public GraphDumper(WorkflowRun run) {

        graph = new HashMap<>();
        allNodes = new HashMap<>();

        this.run = run;

        FlowExecution execution = run.getExecution();

        if (execution != null) {
            ForkScanner.visitSimpleChunks(execution.getCurrentHeads(), this, new StageChunkFinder());
        }
    }

    protected void captureNode(FlowNode node) {
        if (node == null) {
            return;
        }
        allNodes.put(node.getId(), node);
        NodeInfo info = getOrCreateInfo(node.getId());

        for (FlowNode parent : node.getParents()) {
            NodeInfo parentInfo = getOrCreateInfo(parent.getId());
            parentInfo.addChild(info);
            info.addParent(parentInfo);
        }
    }

    private NodeInfo getOrCreateInfo(String id) {
        if (!graph.containsKey(id)) {
            graph.put(id, new NodeInfo(id));
        }
        return graph.get(id);
    }

    @Override
    public void chunkStart(@Nonnull FlowNode startNode, @CheckForNull FlowNode beforeBlock, @Nonnull ForkScanner scanner) {
        captureNode(startNode);
        captureNode(beforeBlock);
    }

    @Override
    public void chunkEnd(@Nonnull FlowNode endNode, @CheckForNull FlowNode afterChunk, @Nonnull ForkScanner scanner) {
        captureNode(endNode);
        captureNode(afterChunk);
    }

    @Override
    public void parallelStart(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchNode, @Nonnull ForkScanner scanner) {
        captureNode(parallelStartNode);
        captureNode(branchNode);
    }

    @Override
    public void parallelEnd(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode parallelEndNode, @Nonnull ForkScanner scanner) {
        captureNode(parallelStartNode);
        captureNode(parallelEndNode);
    }

    @Override
    public void parallelBranchStart(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchStartNode, @Nonnull ForkScanner scanner) {
        captureNode(parallelStartNode);
        captureNode(branchStartNode);
    }

    @Override
    public void parallelBranchEnd(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchEndNode, @Nonnull ForkScanner scanner) {
        captureNode(parallelStartNode);
        captureNode(branchEndNode);
    }

    @Override
    public void atomNode(@CheckForNull FlowNode before, @Nonnull FlowNode atomNode, @CheckForNull FlowNode after, @Nonnull ForkScanner scan) {
        captureNode(before);
        captureNode(atomNode);
        captureNode(after);
    }

    public String getDotGraph() {
        StringBuilder out = new StringBuilder("digraph \"" + run.getDisplayName() + "\" {\n");

        out.append("\tnode [shape=record]\n");
        out.append("\tnode [style=filled]\n");

        for (FlowNode node : allNodes.values()) {

            NodeInfo info = graph.get(node.getId());
            out.append("\n");

            int actionsCount = node.getActions(NodeDownstreamBuildAction.class).size();

            if (node instanceof BlockEndNode) {
                BlockEndNode sn = (BlockEndNode) node;
                out.append(String.format("\t%s -> %s [style=dotted]\n", sn.getStartNode().getId(), sn.getId()));
            }

            attr(out, node, "label",
                 String.format("{{%s|%s}|%s|%d Actions|{par? %s|stg? %s}}",
                               node.getId(),
                               node.getClass().getSimpleName(),
                               node.getDisplayName(),
                               actionsCount,
                               PipelineNodeUtil.isParallelBranch(node),
                               PipelineNodeUtil.isStage(node)
                 ));

            if (info.isFlagged()) {
                attr(out, node, "fillcolor", "#ccddff");
            }

            for (NodeInfo childInfo : info.getChildren()) {
                FlowNode childNode = allNodes.get(childInfo.getNodeId());
                out.append("\t");
                out.append(nodeLabel(node));
                out.append(" -> ");
                out.append(nodeLabel(childNode));
                out.append("\n");
            }
        }

        out.append("}");

        return out.toString();
    }

    private void attr(StringBuilder out, FlowNode node, String name, String value) {
        out.append("\t");
        out.append(nodeLabel(node));
        out.append("[");
        out.append(name);
        out.append("=\"");
        out.append(value);
        out.append("\"]\n");
    }

    private String nodeLabel(FlowNode node) {
        return node.getId();
    }

    public void flagNode(String id) {
        getOrCreateInfo(id).setFlagged(true);
    }

    public static class NodeInfo {

        private final String nodeId;
        private final HashSet<NodeInfo> parents;
        private final HashSet<NodeInfo> children;
        private boolean flagged;

        public NodeInfo(String nodeId) {
            this.nodeId = nodeId;
            this.parents = new HashSet<>();
            this.children = new HashSet<>();
        }

        public void addChild(NodeInfo child) {
            children.add(child);
        }

        public void addParent(NodeInfo parent) {
            parents.add(parent);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NodeInfo nodeInfo = (NodeInfo) o;
            return Objects.equals(nodeId, nodeInfo.nodeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeId);
        }

        public Collection<NodeInfo> getChildren() {
            return Collections.unmodifiableCollection(children);
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setFlagged(boolean flagged) {
            this.flagged = flagged;
        }

        public boolean isFlagged() {
            return flagged;
        }
    }
}
