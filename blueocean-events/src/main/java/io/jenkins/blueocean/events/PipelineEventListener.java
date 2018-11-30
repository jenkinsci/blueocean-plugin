package io.jenkins.blueocean.events;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.Queue;
import hudson.model.Run;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineInputStepListener;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeUtil;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.jenkinsci.plugins.pubsub.Events;
import org.jenkinsci.plugins.pubsub.Message;
import org.jenkinsci.plugins.pubsub.MessageException;
import org.jenkinsci.plugins.pubsub.PubsubBus;
import org.jenkinsci.plugins.pubsub.RunMessage;
import org.jenkinsci.plugins.pubsub.SimpleMessage;
import org.jenkinsci.plugins.workflow.actions.BodyInvocationAction;
import org.jenkinsci.plugins.workflow.actions.QueueItemAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionListener;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graph.StepNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.support.steps.ExecutorStep;
import org.jenkinsci.plugins.workflow.support.steps.input.InputAction;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStep;


/**
 * Listen for run events, filter and publish stage/branch events
 * to notify the UX that there are changes when viewing the
 * pipeline results screen.
 *
 * This may be useful (or may not be needed) for live-ness of the pipeline results screen.
 */
@Extension
public class PipelineEventListener implements GraphListener {

    private static final Logger LOGGER = Logger.getLogger(PipelineEventListener.class.getName());

    private final Cache<FlowExecution, String> currentStageNameCache = CacheBuilder.newBuilder()
                                                                    .weakKeys()
                                                                    .build();
    private final Cache<FlowExecution, String> currentStageIdCache = CacheBuilder.newBuilder()
                                                                    .weakKeys()
                                                                    .build();


    private final ConcurrentMap<FlowExecution, String> currentStageName = currentStageNameCache.asMap();

    private final ConcurrentMap<FlowExecution, String> currentStageId = currentStageIdCache.asMap();


    @Override
    public void onNewHead(FlowNode flowNode) {
        // test whether we have a stage node
        if (PipelineNodeUtil.isStage(flowNode)) {
            List<String> branch = getBranch(flowNode);
            if(flowNode.getDisplayName()!=null) {
                currentStageName.put(flowNode.getExecution(), flowNode.getDisplayName());
            }
            if(flowNode.getId()!=null) {
                currentStageId.put( flowNode.getExecution(), flowNode.getId() );
            }
            publishEvent(newMessage(PipelineEventChannel.Event.pipeline_stage, flowNode, branch));
        } else if (flowNode instanceof StepStartNode) {
            if (flowNode.getAction(BodyInvocationAction.class) != null) {
                List<String> branch = getBranch(flowNode);
                branch.add(flowNode.getId());
                publishEvent(newMessage(PipelineEventChannel.Event.pipeline_block_start, flowNode, branch));
            } else if (flowNode.getPersistentAction(QueueItemAction.class) != null) {
                // Make sure we fire an event for the start of node blocks.
                List<String> branch = getBranch(flowNode);
                publishEvent(newMessage(PipelineEventChannel.Event.pipeline_step, flowNode, branch));
            }
        } else if (flowNode instanceof StepAtomNode) {
            List<String> branch = getBranch(flowNode);
            publishEvent(newMessage(PipelineEventChannel.Event.pipeline_step, flowNode, branch));
        } else if (flowNode instanceof StepEndNode) {
            if (flowNode.getAction(BodyInvocationAction.class) != null) {
                FlowNode startNode = ((StepEndNode) flowNode).getStartNode();
                String startNodeId = startNode.getId();
                List<String> branch = getBranch(startNode);
                branch.add(startNodeId);
                publishEvent(newMessage(PipelineEventChannel.Event.pipeline_block_end, flowNode, branch));
            }
        } else if (flowNode instanceof FlowEndNode) {
            publishEvent(newMessage(PipelineEventChannel.Event.pipeline_end, flowNode.getExecution()));
        }
    }

    /* package: so that we can unit test it */ List<String> getBranch(FlowNode flowNode) {
        return Lists.reverse(flowNode.getAllEnclosingIds());
    }

    private String toPath(Collection<String> branch) {
        StringBuilder builder = new StringBuilder();
        for(String leaf: branch){
            if(builder.length() > 0) {
                builder.append("/");
            }
            builder.append(leaf);
        }
        return builder.toString();
    }

    private static @CheckForNull Run<?, ?> runFor(FlowExecution exec) {
        Queue.Executable executable;
        try {
            executable = exec.getOwner().getExecutable();
        } catch (IOException x) {
            LOGGER.log(Level.WARNING, null, x);
            return null;
        }
        if (executable instanceof Run) {
            return (Run<?, ?>) executable;
        } else {
            return null;
        }
    }

    private static Message newMessage(PipelineEventChannel.Event event, FlowExecution exec) {
        SimpleMessage message = new SimpleMessage()
                .setChannelName(PipelineEventChannel.NAME)
                .setEventName(event);
        Run<?, ?> run = runFor(exec);
        if (run != null) {
            message.set(PipelineEventChannel.EventProps.pipeline_job_name, run.getParent().getFullName())
                   .set(PipelineEventChannel.EventProps.pipeline_run_id, run.getId());
        }
        return message;
    }

    private Message newMessage(PipelineEventChannel.Event event, FlowNode flowNode, Collection<String> branch) {
        Message message = newMessage(event, flowNode.getExecution());

        message.set(PipelineEventChannel.EventProps.pipeline_step_flownode_id, flowNode.getId());
        message.set(PipelineEventChannel.EventProps.pipeline_context, toPath(branch));
        if (currentStageName != null) {
            message.set(PipelineEventChannel.EventProps.pipeline_step_stage_name, currentStageName.get(flowNode.getExecution()));
            message.set(PipelineEventChannel.EventProps.pipeline_step_stage_id, currentStageId.get(flowNode.getExecution()));
        }
        if (flowNode instanceof StepNode) {
            StepNode stepNode = (StepNode) flowNode;
            StepDescriptor stepDescriptor = stepNode.getDescriptor();
            if(stepDescriptor != null) {
                message.set(PipelineEventChannel.EventProps.pipeline_step_name, stepDescriptor.getFunctionName());

                // TODO: Better event choice, more granularity - like only firing when this results in a status change
                if (stepDescriptor instanceof ExecutorStep.DescriptorImpl) {
                    Run<?, ?> run = runFor(flowNode.getExecution());
                    if (run != null) {
                        publishJobEvent(run, Events.JobChannel.job_run_started);
                    }
                    if (flowNode.getPersistentAction(QueueItemAction.class) != null) {
                        // Needed because this is expected everywhere apparently.
                        message.set(PipelineEventChannel.EventProps.pipeline_step_is_paused, String.valueOf(false));
                    }
                }
            }
        }

        if (flowNode instanceof StepAtomNode) {
            Run<?, ?> run = runFor(flowNode.getExecution());
            if (run != null) {
                boolean pausedForInputStep = PipelineNodeUtil
                    .isPausedForInputStep((StepAtomNode) flowNode, run.getAction(InputAction.class));
                if (pausedForInputStep) {
                    // Fire job event to tell we are paused
                    // We will publish on the job channel
                    publishJobEvent(run, Events.JobChannel.job_run_paused);
                }
                message.set(PipelineEventChannel.EventProps.pipeline_step_is_paused, String.valueOf(pausedForInputStep));
            }
        }
        return message;
    }

    private static void publishEvent(Message message) {
        try {
            PubsubBus.getBus().publish(message);
        } catch (MessageException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error publishing pipeline FlowNode event.", e);
        }
    }

    private static void publishJobEvent(@Nonnull Run<?,?> run, @Nonnull Events.JobChannel event) {
        try {
            // TODO: What's the actual event we should send here?
            PubsubBus.getBus().publish(new RunMessage(run)
                .setEventName(event)
            );
        } catch (MessageException e) {
            LOGGER.log(Level.WARNING, "Error publishing Job event.", e);
        }
    }

    @Extension
    public static class StartPublisher extends FlowExecutionListener {

        @Override
        public void onRunning(FlowExecution execution) {
            publishEvent(newMessage(PipelineEventChannel.Event.pipeline_start, execution));
        }

    }

    @Extension
    public static class InputStepPublisher implements PipelineInputStepListener {

        @Override
        public void onStepContinue(InputStep inputStep, WorkflowRun run) {
            // fire an unpaused event in case the input step has received its input
            try {
                PubsubBus.getBus().publish(new RunMessage(run)
                        .setEventName(Events.JobChannel.job_run_unpaused)
                );
            } catch (MessageException e) {
                LOGGER.log(Level.WARNING, "Error publishing Run un-pause event.", e);
            }
        }
    }

}
