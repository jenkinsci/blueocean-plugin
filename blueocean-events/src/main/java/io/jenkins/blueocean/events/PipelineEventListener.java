package io.jenkins.blueocean.events;

import com.google.common.util.concurrent.ListenableFuture;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeUtil;
import org.jenkins.pubsub.Message;
import org.jenkins.pubsub.MessageException;
import org.jenkins.pubsub.PubsubBus;
import org.jenkins.pubsub.SimpleMessage;
import org.jenkinsci.plugins.workflow.actions.BodyInvocationAction;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepNode;
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
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Listen for run events, filter and publish stage/branch events
 * to notify the UX that there are changes when viewing the
 * pipeline results screen.
 *
 * This may be useful (or may not be needed) for live-ness of the pipeline results screen.
 */
@Extension
public class PipelineEventListener extends RunListener<Run<?,?>> {

    private static final Logger LOGGER = Logger.getLogger(PipelineEventListener.class.getName());

    private class StageEventPublisher implements GraphListener {

        private final Run run;
        private final PubsubBus pubSubBus;
        private String currentStageName;
        private String currentStageId;

        public StageEventPublisher(Run r) {
            this.run = r;
            pubSubBus = PubsubBus.getBus();
            publishEvent(newMessage(PipelineEventChannel.Event.pipeline_start));
        }

        @Override
        public void onNewHead(FlowNode flowNode) {
            // test whether we have a stage node
            if (PipelineNodeUtil.isStage(flowNode)) {
                List<String> branch = getBranch(flowNode);
                currentStageName = flowNode.getDisplayName();
                currentStageId = flowNode.getId();
                publishEvent(newMessage(PipelineEventChannel.Event.pipeline_stage, flowNode, branch));
            } else if (flowNode instanceof StepStartNode) {
                if (flowNode.getAction(BodyInvocationAction.class) != null) {
                    List<String> branch = getBranch(flowNode);
                    branch.add(flowNode.getId());
                    publishEvent(newMessage(PipelineEventChannel.Event.pipeline_block_start, flowNode, branch));
                }
            } else if (flowNode instanceof StepAtomNode) {
                List<String> branch = getBranch(flowNode);
                StageAction stageAction = flowNode.getAction(StageAction.class);
                publishEvent(newMessage(PipelineEventChannel.Event.pipeline_step, flowNode, branch));
            } else if (flowNode instanceof StepEndNode) {
                if (flowNode.getAction(BodyInvocationAction.class) != null) {
                    try {
                        String startNodeId = ((StepEndNode) flowNode).getStartNode().getId();
                        FlowNode startNode =  flowNode.getExecution().getNode(startNodeId);
                        List<String> branch = getBranch(startNode);

                        branch.add(startNodeId);
                        publishEvent(newMessage(PipelineEventChannel.Event.pipeline_block_end, flowNode, branch));
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Unexpected error publishing pipeline FlowNode event.", e);
                    }
                }
            } else if (flowNode instanceof FlowEndNode) {
                publishEvent(newMessage(PipelineEventChannel.Event.pipeline_end));
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

        private Message newMessage(PipelineEventChannel.Event event) {
            return new SimpleMessage()
                    .setChannelName(PipelineEventChannel.NAME)
                    .setEventName(event)
                    .set(PipelineEventChannel.EventProps.pipeline_job_name, run.getParent().getFullName())
                    .set(PipelineEventChannel.EventProps.pipeline_run_id, run.getId());
        }

        private Message newMessage(PipelineEventChannel.Event event, FlowNode flowNode, List<String> branch) {
            Message message = newMessage(event);

            message.set(PipelineEventChannel.EventProps.pipeline_step_flownode_id, flowNode.getId());
            message.set(PipelineEventChannel.EventProps.pipeline_context, toPath(branch));
            if (currentStageName != null) {
                message.set(PipelineEventChannel.EventProps.pipeline_step_stage_name, currentStageName);
                message.set(PipelineEventChannel.EventProps.pipeline_step_stage_id, currentStageId);
            }
            if (flowNode instanceof StepNode) {
                StepNode stepNode = (StepNode) flowNode;
                message.set(PipelineEventChannel.EventProps.pipeline_step_name, stepNode.getDescriptor().getFunctionName());
            }

            return message;
        }

        private void publishEvent(Message message) {
            try {
                pubSubBus.publish(message);
            } catch (MessageException e) {
                LOGGER.log(Level.SEVERE, "Unexpected error publishing pipeline FlowNode event.", e);
            }
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
                        LOGGER.log(Level.SEVERE, "Unexpected error publishing pipeline FlowNode event.", e);
                    }
                }
            }, executor);
        }
    }

}
