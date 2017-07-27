package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Run;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueTestResultFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueInputStep;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueTestResult;
import io.jenkins.blueocean.rest.model.BlueTestResultContainer;
import io.jenkins.blueocean.rest.model.BlueTestSummary;
import io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl;
import io.jenkins.blueocean.service.embedded.rest.BlueJUnitTestResult;
import io.jenkins.blueocean.service.embedded.rest.BlueTestResultContainerImpl;
import org.jenkinsci.plugins.workflow.actions.LogAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Implementation of {@link BluePipelineNode}.
 *
 * @author Vivek Pandey
 * @see FlowNode
 */
public class PipelineNodeImpl extends BluePipelineNode {
    private final FlowNodeWrapper node;
    private final List<Edge> edges;
    private final Long durationInMillis;
    private final NodeRunStatus status;
    private final Link self;
    private final WorkflowRun run;

    public PipelineNodeImpl(FlowNodeWrapper node, Link parentLink, WorkflowRun run) {
        this.node = node;
        this.run = run;
        this.edges = buildEdges(node.edges);
        this.status = node.getStatus();
        this.durationInMillis = node.getTiming().getTotalDurationMillis();
        this.self = parentLink.rel(node.getId());
    }

    @Override
    public String getId() {
        return node.getId();
    }

    @Override
    public String getDisplayName() {
        return PipelineNodeUtil.getDisplayName(node.getNode());
    }

    @Override
    public String getDisplayDescription() {
        return null;
    }

    @Override
    public BlueRun.BlueRunResult getResult() {
        return status.getResult();
    }

    @Override
    public BlueRun.BlueRunState getStateObj() {
        return status.getState();
    }

    @Override
    public BlueTestResultContainer getTests() {
        return new BlueTestResultContainerImpl(this, run);
    }

    @Override
    public BlueTestSummary getTestSummary() {
        return BlueTestResultFactory.resolve(run, this).summary;
    }

    @Override
    public Date getStartTime() {
        long nodeTime = node.getTiming().getStartTimeMillis();
        if(nodeTime == 0){
            return null;
        }
        return new Date(nodeTime);
    }

    @Override
    public List<Edge> getEdges() {
        return edges;
    }

    @Override
    public Long getDurationInMillis() {
        return durationInMillis;
    }

    /**
     * Appended logs of steps.
     *
     * @see BluePipelineStep#getLog()
     */
    @Override
    public Object getLog() {
        return new NodeLogResource(this);
    }

    @Override
    public String getCauseOfBlockage() {
        return node.getCauseOfFailure();
    }

    @Override
    public BluePipelineStepContainer getSteps() {
        return new PipelineStepContainerImpl(node, self, run);
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public Collection<BlueActionProxy> getActions() {
        return ActionProxiesImpl.getActionProxies(node.getNode().getActions(), new Predicate<Action>() {
            @Override
            public boolean apply(@Nullable Action input) {
                return input instanceof LogAction;
            }
        }, this);
    }

    @Override
    public BlueInputStep getInputStep() {
        return null;
    }

    @Override
    public HttpResponse submitInputStep(StaplerRequest request) {
        return null;
    }

    public static class EdgeImpl extends Edge{
        private final String id;

        public EdgeImpl(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }
    }

    private List<Edge> buildEdges(List<String> nodes){
        List<Edge> edges  = new ArrayList<>();
        if(!nodes.isEmpty()) {
            for (String id:nodes) {
                edges.add(new EdgeImpl(id));
            }
        }
        return edges;
    }

    FlowNodeWrapper getFlowNodeWrapper(){
        return node;
    }

    @Extension
    public static class NodeFactoryImpl extends BlueTestResultFactory {
        @Override
        public Result getBlueTestResults(Run<?, ?> run, final Reachable parent) {
            Iterable<BlueTestResult> results = null;
            TestResultAction action = run.getAction(TestResultAction.class);
            if (action != null && parent instanceof BluePipelineNode) {
                List<CaseResult> testsToTransform = new ArrayList<>();

                // TODO: node.getSteps() doesn't include block-scoped steps, which could add tests - i.e., withMaven
                TestResult testsForNode = action.getResult().getResultByRunAndNodes(run.getExternalizableId(),
                    ImmutableList.copyOf(Iterables.transform(((BluePipelineNode) parent).getSteps(),
                        new Function<BluePipelineStep, String>() {
                            @Override
                            public String apply(BluePipelineStep step) {
                                return step.getId();
                            }
                        })));
                if (testsForNode != null) {
                    testsToTransform.addAll(testsForNode.getFailedTests());
                    testsToTransform.addAll(testsForNode.getSkippedTests());
                    testsToTransform.addAll(testsForNode.getPassedTests());
                }

                results = Iterables.transform(testsToTransform, new Function<CaseResult, BlueTestResult>() {
                    @Override
                    public BlueTestResult apply(@Nullable CaseResult input) {
                        return new BlueJUnitTestResult(input, parent.getLink());
                    }
                });
            }
            if (results == null) {
                results = ImmutableList.of();
            }
            return Result.of(results);
        }
    }

}
