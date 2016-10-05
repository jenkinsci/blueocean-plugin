package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.FreeStyleProject;
import hudson.model.Project;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.visualization.table.FlowGraphTable;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.MockFolder;

import java.io.IOException;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class LinkResolverTest extends PipelineBaseTest {

    @Override
    public void setup() throws Exception {
        super.setup();
    }

    @Test
    public void nestedFolderJobLinkResolveTest() throws IOException {
        Project f = j.createFreeStyleProject("fstyle1");
        MockFolder folder1 = j.createFolder("folder1");
        Project p1 = folder1.createProject(FreeStyleProject.class, "test1");
        MockFolder folder2 = folder1.createProject(MockFolder.class, "folder2");
        MockFolder folder3 = folder2.createProject(MockFolder.class, "folder3");
        Project p2 = folder2.createProject(FreeStyleProject.class, "test2");
        Project p3 = folder3.createProject(FreeStyleProject.class, "test3");

        WorkflowJob pipelineJob1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        pipelineJob1.setDefinition(new CpsFlowDefinition("stage \"Build\"\n" +
            "    node {\n" +
            "       sh \"echo here\"\n" +
            "    }\n" +
            "\n"));

        WorkflowJob pipelineJob2 = folder2.createProject(WorkflowJob.class, "pipeline2");
        pipelineJob2.setDefinition(new CpsFlowDefinition("stage \"Build\"\n" +
            "    node {\n" +
            "       sh \"echo here\"\n" +
            "    }\n" +
            "\n"));


        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/pipeline1/",LinkResolver.resolveLink(pipelineJob1).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/pipeline2/",LinkResolver.resolveLink(pipelineJob2).getHref());

        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/fstyle1/",LinkResolver.resolveLink(f).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/",LinkResolver.resolveLink(folder1).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/test1/",LinkResolver.resolveLink(p1).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/",LinkResolver.resolveLink(folder2).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/test2/",LinkResolver.resolveLink(p2).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/",LinkResolver.resolveLink(folder3).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/pipelines/test3/",LinkResolver.resolveLink(p3).getHref());
    }


    @Test
    public void resolveNodeLink() throws Exception {
        {
            WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
            job1.setDefinition(new CpsFlowDefinition("stage \"Build\"\n" +
                "    node {\n" +
                "       sh \"echo here\"\n" +
                "    }\n" +
                "\n" +
                "stage \"Test\"\n" +
                "    parallel (\n" +
                "        \"Firefox\" : {\n" +
                "            node {\n" +
                "                sh \"echo ffox\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"Chrome\" : {\n" +
                "            node {\n" +
                "                sh \"echo chrome\"\n" +
                "            }\n" +
                "        }\n" +
                "    )\n" +
                "\n" +
                "stage \"CrashyMcgee\"\n" +
                "  parallel (\n" +
                "    \"SlowButSuccess\" : {\n" +
                "        node {\n" +
                "            echo 'This is time well spent.'\n" +
                "        }\n" +
                "    },\n" +
                "    \"DelayThenFail\" : {\n" +
                "        node {\n" +
                "            echo 'Not yet.'\n" +
                "        }\n" +
                "    }\n" +
                "  )\n" +
                "\n" +
                "\n" +
                "stage \"Deploy\"\n" +
                "    node {\n" +
                "        sh \"echo deploying\"\n" +
                "    }"));

            WorkflowRun b1 = job1.scheduleBuild2(0).get();
            j.assertBuildStatusSuccess(b1);

            FlowGraphTable nodeGraphTable = new FlowGraphTable(b1.getExecution());
            nodeGraphTable.build();
            List<FlowNode> nodes = getStages(nodeGraphTable);
            List<FlowNode> parallelNodes = getParallelNodes(nodeGraphTable);

            Assert.assertEquals(String.format("/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/%s/nodes/%s/",
                b1.getId(),nodes.get(0).getId()),
                LinkResolver.resolveLink(nodes.get(0)).getHref());

            Assert.assertEquals(String.format("/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/%s/nodes/%s/",
                b1.getId(),parallelNodes.get(0).getId()),
                LinkResolver.resolveLink(parallelNodes.get(0)).getHref());

            NodeGraphBuilder graphBuilder = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(b1);

            List<BluePipelineStep> steps = graphBuilder.getPipelineNodeSteps(new Link(String.format("/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/%s/steps/", b1.getId())));

            Assert.assertEquals(String.format("/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/%s/steps/%s/",
                b1.getId(),steps.get(0).getId()),
                LinkResolver.resolveLink(steps.get(0)).getHref());

        }
    }
}
