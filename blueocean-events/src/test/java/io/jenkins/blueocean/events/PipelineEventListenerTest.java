package io.jenkins.blueocean.events;

import hudson.model.Result;
import io.jenkins.blueocean.rest.impl.pipeline.NodeGraphBuilder;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;

import java.util.Arrays;
import java.util.List;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class PipelineEventListenerTest extends PipelineBaseTest{

    @Test
    public void testParentNodesOrder() throws Exception {
        String script = "node {\n" +
                "    stage('one') {\n" +
                "        sh \"echo 42\"        \n" +
                "        parallel('branch1':{\n" +
                "          sh 'echo \"branch1\"'\n" +
                "        }, 'branch2': {\n" +
                "          sh 'echo \"branch2\"'\n" +
                "        })\n" +
                "    }\n" +
                "\n" +
                "}";
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition(script));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.SUCCESS, b1);

        List<FlowNode> parallels = getParallelNodes(NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(b1));
        Assert.assertEquals("10", parallels.get(0).getId());
        Assert.assertEquals("Branch: branch1", parallels.get(0).getDisplayName());

        Assert.assertEquals( Arrays.asList("2", "3", "4", "5", "6", "8"),
                new PipelineEventListener().getBranch(parallels.get(0)));
    }
}
