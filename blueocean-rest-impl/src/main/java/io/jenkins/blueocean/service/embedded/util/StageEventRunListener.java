package io.jenkins.blueocean.service.embedded.util;

import com.google.common.util.concurrent.ListenableFuture;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import org.jenkinsci.plugins.workflow.actions.BodyInvocationAction;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


/**
 * Listen for run events, filter and publish stage/branch events
 * to notify the UX that there are changes when viewing the
 * pipeline results screen.
 *
 * This may be useful (or may not be needed) for live-ness of the pipeline results screen.
 */
@Extension
public class StageEventRunListener extends RunListener<Run<?,?>> {


    private class StageEventPublisher implements GraphListener {

        private final Run run;
        private String currentStageName;

        public StageEventPublisher(Run r) {
            this.run = r;
            System.out.println("***********************************");
            System.out.println("Starting run for " + run.getFullDisplayName());
        }

        @Override
        public void onNewHead(FlowNode flowNode) {
            if (flowNode instanceof StepStartNode) {
                if (flowNode.getAction(BodyInvocationAction.class) != null) {
                    List<String> branch = getBranch(flowNode);
                    branch.add(flowNode.getId());
                    System.out.println("Start block: " + toPath(branch));
                }
            } else if (flowNode instanceof StepAtomNode) {
                List<String> branch = getBranch(flowNode);
                StageAction stageAction = flowNode.getAction(StageAction.class);

                if (stageAction != null) {
                    currentStageName = stageAction.getStageName();
                    System.out.println("Stage: " + currentStageName);
                } else {
                    System.out.println("Step:  " + flowNode.getDisplayName());
                }
                System.out.println("Ctx:   " + toPath(branch));
            } else if (flowNode instanceof StepEndNode) {
                if (flowNode.getAction(BodyInvocationAction.class) != null) {
                    try {
                        String startNodeId = ((StepEndNode) flowNode).getStartNode().getId();
                        FlowNode startNode =  flowNode.getExecution().getNode(startNodeId);
                        List<String> branch = getBranch(startNode);

                        branch.add(startNodeId);
                        System.out.println("End block: " + toPath(branch));
                    } catch (IOException e) {

                    }
                }
            } else if (flowNode instanceof FlowEndNode) {
                System.out.println("Ending run for " + run.getFullDisplayName());
                System.out.println("***********************************");
            }
        }

        private List<String> getBranch(FlowNode flowNode) {
            List<String> branch = new ArrayList<>();
            FlowNode parentBlock = getParentBlock(flowNode);

            while (parentBlock != null) {
                branch.add(0, parentBlock.getId());
                parentBlock = getParentBlock(parentBlock);
            }

            return branch;
        }

        private FlowNode getParentBlock(FlowNode flowNode) {
            List<FlowNode> parents = flowNode.getParents();

            for (FlowNode parent : parents) {
                if (parent instanceof StepStartNode) {
                    if (parent.getAction(BodyInvocationAction.class) != null) {
                        return parent;
                    }
                }
            }

            for (FlowNode parent : parents) {
                if (parent instanceof StepEndNode) {
                    continue;
                }
                FlowNode grandparent = getParentBlock(parent);
                if (grandparent != null) {
                    return grandparent;
                }
            }

            return null;
        }

        private String toPath(List<String> branch) {
            StringBuilder builder = new StringBuilder();
            for (String leaf : branch) {
                if(builder.length() > 0) {
                    builder.append("/");
                }
                builder.append(leaf);
            }
            return builder.toString();
        }
    }

    private ExecutorService executor = new ThreadPoolExecutor(0, 5, 10L,TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    @Override
    public void onStarted(final Run<?,?> run, TaskListener listener) {
        super.onStarted(run, listener);
        if (run instanceof WorkflowRun) {
            ListenableFuture<FlowExecution> promise = ((WorkflowRun) run).getExecutionPromise();
            promise.addListener(new Runnable() {
                @Override
                public void run() {
                    try {

                        FlowExecution ex = ((WorkflowRun) run).getExecutionPromise().get();
                        ex.addListener(new StageEventPublisher(run));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, executor);
        }
    }




}
