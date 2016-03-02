package io.jenkins.blueocean.service.embedded.rest;

import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.BlockEndNode;
import org.jenkinsci.plugins.workflow.graph.BlockStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowGraphWalker;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAG of {@link FlowNode}s
 *
 * @author Vivek Pandey
 */
public class PipelineNodeGraph {
    private final FlowExecution execution;
    /**
     * Point in time snapshot of all the active heads.
     */
    private List<FlowNode> heads;

    private Node root;



    public PipelineNodeGraph(FlowExecution execution) {
        this.execution = execution;
    }

    public void build(){
        if (execution!=null) {
            Map<FlowNode, Node> nodes = createAllNodes();
            this.root = buildForwardReferences(nodes);
        }else{
            this.root = null;
        }
    }


    public Node getRoot(){
        return root;
    }

    private Map<FlowNode, Node> createAllNodes(){
        heads = execution.getCurrentHeads();
        FlowGraphWalker walker = new FlowGraphWalker();
        walker.addHeads(heads);

        // nodes that we've visited
        Map<FlowNode,Node> nodes = new LinkedHashMap<>();
        for (FlowNode n : walker) {
            Node node = new Node(n);
            nodes.put(n, node);
        }
        return nodes;
    }

    /**
     * Builds up forward graph edge references from {@link FlowNode#getParents()} back pointers.
     */
    private Node buildForwardReferences(Map<FlowNode, Node> nodes) {
        // build up all the forward references
        Node root = null;
        for (Node n : nodes.values()) {
            System.out.println(n.getValue().getDisplayName());
            FlowNode fn = n.node;
            for (FlowNode p : fn.getParents()) {
                System.out.println("parent: "+p.getDisplayName());
                System.out.println("child: "+n.getValue().getDisplayName());
                nodes.get(p).addChild(nodes.get(fn));
            }
            if (n.getValue().getParents().isEmpty()) {
                if (root==null)
                    root = n;
                else {
                    // in an unlikely case when we find multiple head nodes,
                    // treat them all as siblings
                    System.out.println("Adding root child: "+n.getValue().getDisplayName());
                    root.addChild(n);
                }
            }

            if (n.isEnd()) {
                BlockEndNode en = (BlockEndNode) n.node;
                Node sr = nodes.get(en.getStartNode());

                assert sr.endNode==null : "start/end mapping should be 1:1";
                sr.endNode = en;
            }
        }
        // graph shouldn't contain any cycle, so there should be at least one 'head node'
        assert root!=null;
        return root;
    }

    public static class Node{
        private final FlowNode node;
        private FlowNode endNode;
        private List<Node> children = new ArrayList<>();


        public Node(FlowNode node) {
            this.node = node;
        }

        public void addChild(FlowNode node){
            children.add(new Node(node));
        }

        public void addChild(Node node){
            children.add(node);
        }
        public List<Node> getChildren(){
            return Collections.unmodifiableList(children);
        }

        public FlowNode getValue(){
            return node;
        }

        boolean isStart() {
            return node instanceof BlockStartNode;
        }

        boolean isEnd() {
            return node instanceof BlockEndNode;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Node && node.equals(((Node)obj).getValue());
        }

        @Override
        public int hashCode() {
            return node.hashCode();
        }
    }


}
