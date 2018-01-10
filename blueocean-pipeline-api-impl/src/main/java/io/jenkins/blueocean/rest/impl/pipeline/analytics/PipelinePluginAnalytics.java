package io.jenkins.blueocean.rest.impl.pipeline.analytics;

import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.blueocean.analytics.Analytics;
import io.jenkins.blueocean.analytics.Analytics.TrackRequest;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.NodeGraphBuilder;
import io.jenkins.blueocean.service.embedded.analytics.Tally;
import org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobAction;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.Nonnull;
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
        // Tally up all the steps used in this run
        Tally tally = new Tally();
        NodeGraphBuilder builder = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(workflowRun);
        builder.getPipelineNodeSteps(new Link("steps/")).forEach(step -> {
            tally.count(step.getStepType());
        });
        boolean isDeclarative = workflowRun.getParent().getAction(DeclarativeJobAction.class) != null;
        Result result = workflowRun.getResult();
        String resultAsString = result != null ? result.toString() : "UNKNOWN";
        // Send event for each step used in this run
        tally.get().forEach((key, value) -> {
            ImmutableMap.Builder<String, Object> props = ImmutableMap.builder();
            props.put("type", key);
            props.put("timesUsed", value);
            props.put("isDeclarative", isDeclarative);
            props.put("runResult", resultAsString);
            analytics.track(new TrackRequest("pipeline_step_used", props.build()));
        });
    }
}
