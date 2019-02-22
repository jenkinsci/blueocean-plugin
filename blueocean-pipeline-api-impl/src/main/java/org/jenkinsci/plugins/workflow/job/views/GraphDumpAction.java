/*
 * The MIT License
 *
 * Copyright 2014 Jesse Glick.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.workflow.job.views;

import hudson.Extension;
import hudson.model.Action;
import hudson.util.HttpResponses;
import jenkins.model.TransientActionFactory;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.workflow.graph.BlockEndNode;
import org.jenkinsci.plugins.workflow.graph.BlockStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowGraphWalker;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graph.StepNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.kohsuke.stapler.HttpResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * Dumps out a JSON representation of an execution's workflow graph
 */
public final class GraphDumpAction implements Action {

    public final WorkflowRun run;

    private GraphDumpAction(WorkflowRun run) {
        this.run = run;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "graphDump";
    }

    public HttpResponse doIndex() throws IOException {

        JSONArray response = new JSONArray();

        FlowGraphWalker walker = new FlowGraphWalker(run.getExecution());
        for (FlowNode node : walker) {
            JSONObject outputNode = new JSONObject();
            outputNode.put("id", node.getId());
            outputNode.put("name", node.getDisplayName());
            outputNode.put("functionName", node.getDisplayFunctionName());
            outputNode.put("className", node.getClass().getName());
            outputNode.put("enclosingId", node.getEnclosingId());
            outputNode.put("isBegin", node instanceof BlockStartNode);
            outputNode.put("isEnd", node instanceof BlockEndNode);
            outputNode.put("isStepNode", node instanceof StepNode);

            if (node instanceof StepNode) {
                StepNode sn = (StepNode) node;
                StepDescriptor descriptor = sn.getDescriptor();
                if (descriptor != null) {
                    JSONObject outputDescriptor = new JSONObject();
                    outputDescriptor.put("getDisplayName", descriptor.getDisplayName());
                    outputDescriptor.put("getFunctionName", descriptor.getFunctionName());
                    outputNode.put("stepDescriptor", outputDescriptor);
                }
            }

            JSONArray parents = new JSONArray();
            for (FlowNode parent : node.getParents()) {
                parents.add(parent.getId());
            }
            outputNode.put("parents", parents);

            if (node instanceof BlockStartNode) {
                BlockStartNode startNode = (BlockStartNode) node;
                final BlockEndNode endNode = startNode.getEndNode();
                outputNode.put("endNodeId", endNode == null ? null : endNode.getId());
            } else if (node instanceof BlockEndNode) {
                BlockEndNode endNode = (BlockEndNode) node;
                outputNode.put("startNodeId", endNode.getStartNode().getId());
            }

            JSONArray actions = new JSONArray();
            for (Action action : node.getAllActions()) {
                JSONObject outputAction = new JSONObject();
                outputAction.put("className", action.getClass().getName());
                outputAction.put("displayName", action.getDisplayName());
                actions.add(outputAction);
            }
            outputNode.put("actions", actions);

            response.add(outputNode);
        }

        return HttpResponses.okJSON(response);
    }

    @Extension
    public static final class Factory extends TransientActionFactory<WorkflowRun> {

        @Override
        public Class<WorkflowRun> type() {
            return WorkflowRun.class;
        }

        @Override
        public Collection<? extends Action> createFor(WorkflowRun run) {
            return Collections.singleton(new GraphDumpAction(run));
        }
    }
}
