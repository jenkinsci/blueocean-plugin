package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import hudson.model.Result;
import io.jenkins.blueocean.listeners.NodeDownstreamBuildAction;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.test.steps.SemaphoreStep;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * These tests are for regressions in the Graph Builder code, to make sure the same input produces the same output nodes
 * and connections over time. We're not trying to excercise Pipeline edge cases, but edge cases for the code that
 * simplifies the complete Pipeline graph to the cut-down Blue Ocean graph. Jobs that run to completion or failure, and
 * produce a working Pipeline DAG. Not for testing HTTP infrastructure or Stapler's JSON code.
 */
public class GraphBuilderTest extends PipelineBaseTest {

    @Test
    public void jenkins53311() throws Exception {
        WorkflowRun run = createAndRunJob("JENKINS-53311", "JENKINS-53311.jenkinsfile");
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "Parallel", "Nested A", "Nested B");
        assertStageAndEdges(nodes, "Nested A");
        assertStageAndEdges(nodes, "Nested B", "Nested B-1");
        assertStageAndEdges(nodes, "Nested B-1");

        assertEquals("Unexpected stages in graph", 4, nodes.size());
    }

    @Test
    @Issue("JENKINS-56383")
    public void multipleParallelsRegression() throws Exception {
        WorkflowRun run = createAndRunJob("JENKINS-56383", "JENKINS-56383.jenkinsfile");
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "Top1", "TOP1-P1", "TOP1-P2");
        assertStageAndEdges(nodes, "TOP1-P1", "TOP2");
        assertStageAndEdges(nodes, "TOP1-P2", "TOP2");

        assertStageAndEdges(nodes, "TOP2", "TOP2-P1", "TOP2-P2");
        assertStageAndEdges(nodes, "TOP2-P1", "TOP3");
        assertStageAndEdges(nodes, "TOP2-P2", "TOP3");

        assertStageAndEdges(nodes, "TOP3");

        assertEquals("Unexpected stages in graph", 7, nodes.size());
    }

    @Test
    public void declarativeQueuedAgent() throws Exception {
        WorkflowRun run = createAndRunJob("declarativeQueuedAgent", "declarativeQueuedAgent.jenkinsfile");
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "yo");

        assertEquals("Unexpected stages in graph", 1, nodes.size());
    }

    @Test
    public void declarativeThreeStages() throws Exception {
        WorkflowRun run = createAndRunJob("declarativeThreeStages", "declarativeThreeStages.jenkinsfile");
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "first", "second");
        assertStageAndEdges(nodes, "second", "third");
        assertStageAndEdges(nodes, "third");

        assertEquals("Unexpected stages in graph", 3, nodes.size());
    }

    @Test
    public void downstreamBuildLinks() throws Exception {

        // Any simple pipeline would do for these
        createJob("downstream1", "declarativeQueuedAgent.jenkinsfile");
        createJob("downstream2", "declarativeQueuedAgent.jenkinsfile");

        WorkflowRun run = createAndRunJob("downstreamBuildLinks", "downstreamBuildLinks.jenkinsfile");
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "Stage the first", "Stage the second");
        assertStageAndEdges(nodes, "Stage the second", "downstream1", "downstream2");
        FlowNodeWrapper ds1Node = assertStageAndEdges(nodes, "downstream1", "Double-downstream");
        FlowNodeWrapper ds2Node = assertStageAndEdges(nodes, "downstream2", "Double-downstream");
        FlowNodeWrapper ddsNode = assertStageAndEdges(nodes, "Double-downstream");

        assertEquals("Unexpected stages in graph", 5, nodes.size());

        Collection<NodeDownstreamBuildAction> actions = ds1Node.getPipelineActions(NodeDownstreamBuildAction.class);

        assertEquals("downstream1 stage built downstream1", 1,
                     ds1Node.getPipelineActions(NodeDownstreamBuildAction.class).stream()
                            .filter(action -> action.getLink().getHref().contains("downstream1"))
                            .count());

        assertEquals("downstream2 stage built downstream2", 1,
                     ds2Node.getPipelineActions(NodeDownstreamBuildAction.class).stream()
                            .filter(action -> action.getLink().getHref().contains("downstream2"))
                            .count());

        assertEquals("Double-downstream stage built downstream1", 1,
                     ddsNode.getPipelineActions(NodeDownstreamBuildAction.class).stream()
                            .filter(action -> action.getLink().getHref().contains("downstream1"))
                            .count());

        assertEquals("Double-downstream stage built downstream2", 1,
                     ddsNode.getPipelineActions(NodeDownstreamBuildAction.class).stream()
                            .filter(action -> action.getLink().getHref().contains("downstream2"))
                            .count());
    }

    @Test
    public void downstreamBuildLinksDecl() throws Exception {

        // Any simple pipeline would do for these
        createJob("downstream1", "declarativeQueuedAgent.jenkinsfile");
        createJob("downstream2", "declarativeQueuedAgent.jenkinsfile");

        WorkflowRun run = createAndRunJob("downstreamBuildLinksDecl", "downstreamBuildLinksDecl.jenkinsfile");
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "Stage the first", "Stage the second");
        assertStageAndEdges(nodes, "Stage the second", "downstream1", "downstream2");
        FlowNodeWrapper ds1Node = assertStageAndEdges(nodes, "downstream1", "Double-downstream");
        FlowNodeWrapper ds2Node = assertStageAndEdges(nodes, "downstream2", "Double-downstream");
        FlowNodeWrapper ddsNode = assertStageAndEdges(nodes, "Double-downstream");

        assertEquals("Unexpected stages in graph", 5, nodes.size());

        Collection<NodeDownstreamBuildAction> actions = ds1Node.getPipelineActions(NodeDownstreamBuildAction.class);

        assertEquals("downstream1 stage built downstream1", 1,
                     ds1Node.getPipelineActions(NodeDownstreamBuildAction.class).stream()
                            .filter(action -> action.getLink().getHref().contains("downstream1"))
                            .count());

        assertEquals("downstream2 stage built downstream2", 1,
                     ds2Node.getPipelineActions(NodeDownstreamBuildAction.class).stream()
                            .filter(action -> action.getLink().getHref().contains("downstream2"))
                            .count());

        assertEquals("Double-downstream stage built downstream1", 1,
                     ddsNode.getPipelineActions(NodeDownstreamBuildAction.class).stream()
                            .filter(action -> action.getLink().getHref().contains("downstream1"))
                            .count());

        assertEquals("Double-downstream stage built downstream2", 1,
                     ddsNode.getPipelineActions(NodeDownstreamBuildAction.class).stream()
                            .filter(action -> action.getLink().getHref().contains("downstream2"))
                            .count());
    }

    @Test
    public void downstreamBuildLinksSeq() throws Exception {

        // Any simple pipeline would do for these
        createJob("downstream1", "declarativeQueuedAgent.jenkinsfile");
        createJob("downstream2", "declarativeQueuedAgent.jenkinsfile");
        createJob("downstream3", "declarativeQueuedAgent.jenkinsfile");
        createJob("downstream4", "declarativeQueuedAgent.jenkinsfile");

        WorkflowRun run = createAndRunJob("downstreamBuildLinksSeq", "downstreamBuildLinksSeq.jenkinsfile");
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "Stage the first", "Stage the second");
        assertStageAndEdges(nodes, "Stage the second", "Outer", "Single stage branch", "Two stage branch");
        assertStageAndEdges(nodes, "Outer", "Inner");
        FlowNodeWrapper ssb = assertStageAndEdges(nodes, "Single stage branch", "Final stage");
        assertStageAndEdges(nodes, "Two stage branch", "build-ds3");
        assertStageAndEdges(nodes, "Final stage");
        FlowNodeWrapper inner = assertStageAndEdges(nodes, "Inner", "Final stage");
        FlowNodeWrapper bds3 = assertStageAndEdges(nodes, "build-ds3", "build-ds4");
        FlowNodeWrapper bds4 = assertStageAndEdges(nodes, "build-ds4", "Final stage");

        assertEquals("Unexpected stages in graph", 9, nodes.size());

        assertEquals("ssb stage built downstream1", 1,
                     ssb.getPipelineActions(NodeDownstreamBuildAction.class).stream()
                        .filter(action -> action.getLink().getHref().contains("downstream1"))
                        .count());

        assertEquals("Inner stage built downstream2", 1,
                     inner.getPipelineActions(NodeDownstreamBuildAction.class).stream()
                          .filter(action -> action.getLink().getHref().contains("downstream2"))
                          .count());

        assertEquals("bds3 stage built downstream3", 1,
                     bds3.getPipelineActions(NodeDownstreamBuildAction.class).stream()
                         .filter(action -> action.getLink().getHref().contains("downstream3"))
                         .count());

        assertEquals("bds4 stage built downstream4", 1,
                     bds4.getPipelineActions(NodeDownstreamBuildAction.class).stream()
                         .filter(action -> action.getLink().getHref().contains("downstream4"))
                         .count());
    }

    @Test
    @Issue("JENKINS-39203")
    public void sillyLongName() throws Exception {
        WorkflowRun run = createAndRunJob("SillyLongName",
                                          "earlyUnstableStatusShouldReportPunStateAsRunningAndResultAsUnknown.jenkinsfile",
                                          Result.UNSTABLE);
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "stage 1 marked as unstable", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.UNSTABLE, "stage 2 wait");
        assertStageAndEdges(nodes, "stage 2 wait", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.SUCCESS);

        assertEquals("Unexpected stages in graph", 2, nodes.size());
    }

    @Test
    public void nestedStagesGroups() throws Exception {
        WorkflowRun run = createAndRunJob("nestedStagesGroups", "nestedStagesGroups.jenkinsfile");
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "top", "first");
        assertStageAndEdges(nodes, "first", "first-inner-first");
        assertStageAndEdges(nodes, "first-inner-first", "first-inner-second");
        assertStageAndEdges(nodes, "first-inner-second", "second");
        assertStageAndEdges(nodes, "second", "second-inner-first");
        assertStageAndEdges(nodes, "second-inner-first", "second-inner-second");
        assertStageAndEdges(nodes, "second-inner-second", BlueRun.BlueRunState.SKIPPED, BlueRun.BlueRunResult.NOT_BUILT);

        assertEquals("Unexpected stages in graph", 7, nodes.size());
    }

    @Test
    public void parallelStagesGroupsAndStages() throws Exception {
        WorkflowRun run = createAndRunJob("parallelStagesGroupsAndStages", "parallelStagesGroupsAndStages.jenkinsfile");
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "top", "first", "second");
        assertStageAndEdges(nodes, "first", "first-inner-first");
        assertStageAndEdges(nodes, "second", "second-inner-first");
        assertStageAndEdges(nodes, "first-inner-first", "first-inner-second");
        assertStageAndEdges(nodes, "first-inner-second");
        assertStageAndEdges(nodes, "second-inner-first", "second-inner-second");
        assertStageAndEdges(nodes, "second-inner-second");

        assertEquals("Unexpected stages in graph", 7, nodes.size());
    }

    @Test
    public void parallelStagesNonNested() throws Exception {
        WorkflowRun run = createAndRunJob("parallelStagesNonNested", "parallelStagesNonNested.jenkinsfile");
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "top", "first", "second");
        assertStageAndEdges(nodes, "first");
        assertStageAndEdges(nodes, "second");

        assertEquals("Unexpected stages in graph", 3, nodes.size());
    }

    @Test
    @Issue("JENKINS-43292")
    public void parallelFailFast() throws Exception {
        WorkflowRun run = createAndRunJob("parallelFailFast", "parallelFailFast.jenkinsfile", Result.FAILURE);
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "Parallel", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.FAILURE, "aborts", "fails", "succeeds");
        assertStageAndEdges(nodes, "aborts", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.ABORTED);
        assertStageAndEdges(nodes, "fails", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.FAILURE);
        assertStageAndEdges(nodes, "succeeds", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.SUCCESS);

        assertEquals("Unexpected stages in graph", 4, nodes.size());
    }

    @Test
    @Issue("JENKINS-43292")
    public void parallelFailFastDeclarative() throws Exception {
        WorkflowRun run = createAndRunJob("parallelFailFastDeclarative", "parallelFailFastDeclarative.jenkinsfile", Result.FAILURE);
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        // top should be BlueRunResult.FAILURE, but the status is currently computed from the beginning of top to the
        // beginning of aborts, which doesn't make sense, but we don't show a status for parents of sequential stages
        // anyway so it doesn't really matter.
        assertStageAndEdges(nodes, "top", "aborts", "fails", "succeeds");
        assertStageAndEdges(nodes, "aborts", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.ABORTED);
        assertStageAndEdges(nodes, "fails", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.FAILURE);
        assertStageAndEdges(nodes, "succeeds", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.SUCCESS);

        assertEquals("Unexpected stages in graph", 4, nodes.size());
    }

    @Test
    public void restartStage() throws Exception {

        WorkflowRun run = createAndRunJob("restartStage", "restartStage.jenkinsfile");
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "Build", "Browser Tests");
        assertStageAndEdges(nodes, "Browser Tests", "Chrome", "Firefox", "Internet Explorer", "Safari");
        assertStageAndEdges(nodes, "Chrome", "Static Analysis");
        assertStageAndEdges(nodes, "Firefox", "Static Analysis");
        assertStageAndEdges(nodes, "Internet Explorer", "Static Analysis");
        assertStageAndEdges(nodes, "Safari", "Static Analysis");
        assertStageAndEdges(nodes, "Static Analysis", "Deploy");
        assertStageAndEdges(nodes, "Deploy", "DeployX", "final");
        assertStageAndEdges(nodes, "DeployX");
        assertStageAndEdges(nodes, "final");

        assertEquals("Unexpected stages in graph", 10, nodes.size());
    }

    @Test
    public void sequentialParallel() throws Exception {
        WorkflowRun run = createAndRunJob("sequentialParallel", "sequentialParallel.jenkinsfile");
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "first-solo", "parent");
        assertStageAndEdges(nodes, "parent", "multiple-stages", "other-single-stage", "single-stage");
        assertStageAndEdges(nodes, "multiple-stages", "first-sequential-stage");
        assertStageAndEdges(nodes, "other-single-stage", "second-solo");
        assertStageAndEdges(nodes, "single-stage", "second-solo");
        assertStageAndEdges(nodes, "second-solo");
        assertStageAndEdges(nodes, "first-sequential-stage", "second-sequential-stage");
        assertStageAndEdges(nodes, "second-sequential-stage", "third-sequential-stage");
        assertStageAndEdges(nodes, "third-sequential-stage", "second-solo");

        assertEquals("Unexpected stages in graph", 9, nodes.size());
    }

    @Test
    public void sequentialParallelWithPost() throws Exception {
        WorkflowRun run = createAndRunJob("sequentialParallelWithPost", "sequentialParallelWithPost.jenkinsfile");
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "first-solo", "parent");
        assertStageAndEdges(nodes, "parent", "multiple-stages", "other-single-stage", "single-stage");
        assertStageAndEdges(nodes, "multiple-stages", "first-sequential-stage");
        assertStageAndEdges(nodes, "other-single-stage", "second-solo");
        assertStageAndEdges(nodes, "single-stage", "second-solo");
        assertStageAndEdges(nodes, "second-solo");
        assertStageAndEdges(nodes, "first-sequential-stage", "second-sequential-stage");
        assertStageAndEdges(nodes, "second-sequential-stage", "third-sequential-stage");
        assertStageAndEdges(nodes, "third-sequential-stage", "second-solo");

        assertEquals("Unexpected stages in graph", 9, nodes.size());
    }

    @Test
    public void secondStageFails() throws Exception {
        WorkflowRun run = createAndRunJob("secondStageFails", "successfulStepWithBlockFailureAfterward.jenkinsfile", Result.FAILURE);
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "first", "second");
        assertStageAndEdges(nodes, "second", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.FAILURE);

        assertEquals("Unexpected stages in graph", 2, nodes.size());
    }

    @Test
    public void testDynamicInnerStage() throws Exception {
        WorkflowRun run = createAndRunJob("testDynamicInnerStage", "testDynamicInnerStage.jenkinsfile");
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "test", "parallel stage");
        assertStageAndEdges(nodes, "parallel stage", "a_1");
        assertStageAndEdges(nodes, "a_1", "test2");
        assertStageAndEdges(nodes, "test2");

        assertEquals("Unexpected stages in graph", 4, nodes.size());
    }

    @Test
    public void unstableSmokes() throws Exception {
        WorkflowRun run = createAndRunJob("unstableSmokes", "unstableSmokes.jenkinsfile", Result.FAILURE);
        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();

        assertStageAndEdges(nodes, "unstable-one", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.UNSTABLE, "success");
        assertStageAndEdges(nodes, "success", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.SUCCESS, "unstable-two");
        assertStageAndEdges(nodes, "unstable-two", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.UNSTABLE, "failure");
        assertStageAndEdges(nodes, "failure", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.FAILURE);

        assertEquals("Unexpected stages in graph", 4, nodes.size());
    }

    /**
     * Builds a basic Pipeline where the overall graph structure (in terms of stages) is the same from one build
     * to the next, but there are differences in the number of FlowNodes (and thus different node IDs for
     * corresponding nodes like the starts of stages) in each build.
     *
     * If this test breaks, it probably means that something is mixing up node IDs across different builds when it
     * should not be doing so.
     */
    @Test
    public void unionDifferentNodeIdsSameStructure() throws Exception {
        WorkflowJob p = createJob("unionDifferentNodeIdsSameStructure", "unionDifferentNodeIdsSameStructure.jenkinsfile");
        // Run the first build, it should complete successfully. We don't care about its structure.   
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        SemaphoreStep.waitForStart("second/1", b1);
        SemaphoreStep.success("second/1", null);
        SemaphoreStep.waitForStart("third/1", b1);
        SemaphoreStep.success("third/1", null);
        j.waitForCompletion(b1);
        j.assertBuildStatus(Result.SUCCESS, b1);
        // Run the second build. Its graph as computed by PipelineNodeGraphVisitor will be created by combining the current build with the previous build.
        WorkflowRun b2 = p.scheduleBuild2(0).waitForStart();
        SemaphoreStep.waitForStart("second/2", b2);
        {
            List<FlowNodeWrapper> nodes = unionPipelineNodes(b1, b2);
            assertStageAndEdges(nodes, "first", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.SUCCESS, "second");
            assertStageAndEdges(nodes, "second", BlueRun.BlueRunState.RUNNING, BlueRun.BlueRunResult.UNKNOWN, "third");
            assertStageAndEdges(nodes, "third", null, (BlueRun.BlueRunResult) null);
        }
        SemaphoreStep.success("second/2", null);
        SemaphoreStep.waitForStart("third/2", b2);
        {
            List<FlowNodeWrapper> nodes = unionPipelineNodes(b1, b2);
            assertStageAndEdges(nodes, "first", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.SUCCESS, "second");
            assertStageAndEdges(nodes, "second", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.SUCCESS, "third");
            assertStageAndEdges(nodes, "third", BlueRun.BlueRunState.RUNNING, BlueRun.BlueRunResult.UNKNOWN);
        }
        SemaphoreStep.success("third/2", null);
        j.waitForCompletion(b2);
        j.assertBuildStatus(Result.SUCCESS, b2);
        {
            List<FlowNodeWrapper> nodes = unionPipelineNodes(b1, b2);
            assertStageAndEdges(nodes, "first", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.SUCCESS, "second");
            assertStageAndEdges(nodes, "second", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.SUCCESS, "third");
            assertStageAndEdges(nodes, "third", BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.SUCCESS);
        }
    }

    private FlowNodeWrapper assertStageAndEdges(Collection<FlowNodeWrapper> searchNodes, String stageName, String... edgeNames) {
        return assertStageAndEdges(searchNodes, stageName, BlueRun.BlueRunState.FINISHED, BlueRun.BlueRunResult.SUCCESS, edgeNames);
    }

    private FlowNodeWrapper assertStageAndEdges(Collection<FlowNodeWrapper> searchNodes, String stageName,
                                                BlueRun.BlueRunState expectedState,
                                                BlueRun.BlueRunResult expectedResult,
                                                String... edgeNames) {

        FlowNodeWrapper stage = null;
        for (FlowNodeWrapper node : searchNodes) {
            if (StringUtils.equals(node.getDisplayName(), stageName)) {
                stage = node;
                break;
            }
        }

        if (stage == null) {
            Assert.fail("could not find stage named \"" + stageName + "\"");
        }

        assertEquals("stage state", expectedState, stage.getStatus().state);
        assertEquals("stage result", expectedResult, stage.getStatus().result);

        for (String edgeName : edgeNames) {
            boolean found = false;
            for (FlowNodeWrapper toNode : stage.edges) {
                if (StringUtils.equals(toNode.getDisplayName(), edgeName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Assert.fail(String.format("Stage \"%s\" should be connected to stage \"%s\"", stageName, edgeName));
            }
        }

        assertEquals(String.format("Too many edges from \"%s\".", stageName), edgeNames.length, stage.edges.size());

        return stage;
    }

    // NB: Use the following as a template when you're adding new regression tests from a known-good state

    //    @Test
    //    public void xxxxxx() throws Exception {
    //        WorkflowRun run = createAndRunJob("xxxxxx", "xxxxxx.jenkinsfile");
    //        NodeGraphBuilder graph = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
    //        List<FlowNodeWrapper> nodes = graph.getPipelineNodes();
    //
    //        dumpAssertions(nodes); // TODO: replace with assertions :D
    //
    //        assertEquals("Unexpected stages in graph", 999, nodes.size());
    //    }
    //
    //    private void dumpAssertions(Collection<FlowNodeWrapper> nodes) {
    //        for (FlowNodeWrapper fromNode : nodes) {
    //            if (fromNode.edges.size() == 0) {
    //                // Simple is-present assertion
    //                System.out.println(String.format("assertStageAndEdges(nodes, \"%s\");", fromNode.getDisplayName()));
    //            } else {
    //                // Need edge name params
    //                String edgeNamesList =
    //                    fromNode.edges.stream()
    //                                  .map(edgeNode -> String.format("\"%s\"", edgeNode.getDisplayName()))
    //                                  .collect(Collectors.joining(", "));
    //                System.out.println(String.format("assertStageAndEdges(nodes, \"%s\", %s);",
    //                                                 fromNode.getDisplayName(),
    //                                                 edgeNamesList));
    //            }
    //        }
    //    }

    private WorkflowRun createAndRunJob(String jobName, String jenkinsFileName) throws Exception {
        return createAndRunJob(jobName, jenkinsFileName, Result.SUCCESS);
    }

    private WorkflowRun createAndRunJob(String jobName, String jenkinsFileName, Result expectedResult) throws Exception {
        WorkflowJob job = createJob(jobName, jenkinsFileName);
        j.assertBuildStatus(expectedResult, job.scheduleBuild2(0));
        return job.getLastBuild();
    }

    private WorkflowJob createJob(String jobName, String jenkinsFileName) throws java.io.IOException {
        WorkflowJob job = j.createProject(WorkflowJob.class, jobName);

        URL resource = Resources.getResource(getClass(), jenkinsFileName);
        String jenkinsFile = Resources.toString(resource, Charsets.UTF_8);
        job.setDefinition(new CpsFlowDefinition(jenkinsFile, true));
        return job;
    }

    /**
     * Compute the union of the specified builds using {@link PipelineNodeGraphVisitor#union}.
     * @param b1 the old build.
     * @param b2 the current build.
     * @return the union of the builds converted to {@code List<FlowNodeWrapper>} to be used with methods like {@link #assertStageAndEdges}.
     */
    private List<FlowNodeWrapper> unionPipelineNodes(WorkflowRun b1, WorkflowRun b2) {
        List<FlowNodeWrapper> oldNodes = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(b1).getPipelineNodes();
        return NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(b2)
                    .union(oldNodes, new Link("unused"))
                    .stream()
                    .map(bpn -> ((PipelineNodeImpl) bpn).getFlowNodeWrapper())
                    .collect(Collectors.toList());
    }
}
