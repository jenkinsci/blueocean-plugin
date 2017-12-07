package io.jenkins.blueocean.rest.impl.pipeline.analytics;

import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.blueocean.analytics.Analytics;
import io.jenkins.blueocean.analytics.Analytics.TrackRequest;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.NodeGraphBuilder;
import io.jenkins.blueocean.service.embedded.analytics.Tally;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graph.StepNode;
import org.jenkinsci.plugins.workflow.graphanalysis.ForkScanner;
import org.jenkinsci.plugins.workflow.graphanalysis.StandardChunkVisitor;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.StageChunkFinder;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Extension
@Restricted(NoExternalUse.class)
public class PipelinePluginAnalytics extends RunListener<WorkflowRun> {
    @Override
    public void onCompleted(WorkflowRun workflowRun, @Nonnull TaskListener listener) {
        Analytics analytics = Analytics.get();
        if (analytics == null) {
            return;
        }
        FlowExecution execution = workflowRun.getExecution();
        if (execution == null) {
            return;
        }
        // Tally up all the steps used in this run
        PipelineStepVisitor visitor = new PipelineStepVisitor();
        ForkScanner.visitSimpleChunks(execution.getCurrentHeads(), visitor, new StageChunkFinder());

        boolean isDeclarative = workflowRun.getParent().getAction(DeclarativeJobAction.class) != null;
        Result result = workflowRun.getResult();
        String resultAsString = result != null ? result.toString() : "UNKNOWN";
        // Send event for each step used in this run
        visitor.tally.get().forEach((key, value) -> {
            ImmutableMap.Builder<String, Object> props = ImmutableMap.builder();
            props.put("type", key);
            props.put("timesUsed", value);
            props.put("isDeclarative", isDeclarative);
            props.put("runResult", resultAsString);
            String pluginName = visitor.stepToPlugin.get(key);
            props.put("plugin", pluginName != null ? pluginName : "unknown");
            analytics.track(new TrackRequest("pipeline_step_used", props.build()));
        });
    }

    private class PipelineStepVisitor extends StandardChunkVisitor {
        final Tally tally = new Tally();
        final Map<String, String> stepToPlugin = new HashMap<>();

        @Override
        public void atomNode(@CheckForNull FlowNode before, @Nonnull FlowNode atomNode, @CheckForNull FlowNode after, @Nonnull ForkScanner scan) {
            super.atomNode(before, atomNode, after, scan);
            if (atomNode instanceof StepNode && !(atomNode instanceof StepEndNode)) {
                StepNode stepNode = (StepNode) atomNode;
                StepDescriptor descriptor = stepNode.getDescriptor();
                if (descriptor != null) {
                    String stepType = descriptor.getId();
                    tally.count(stepType);
                    if (descriptor.clazz != null && !stepToPlugin.containsKey(stepType)) {
                        PluginWrapper wrapper = Jenkins.getInstance().getPluginManager().whichPlugin(descriptor.clazz);
                        if (wrapper != null) {
                            stepToPlugin.put(stepType, wrapper.getShortName());
                        }
                    }
                }
            }
        }
    }
}
