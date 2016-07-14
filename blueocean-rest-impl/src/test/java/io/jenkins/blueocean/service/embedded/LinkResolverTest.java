package io.jenkins.blueocean.service.embedded;

import com.google.inject.Inject;
import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.model.Run;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import io.jenkins.blueocean.service.embedded.rest.PipelineNodeGraphBuilder;
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
import java.util.concurrent.ExecutionException;

/**
 * @author Vivek Pandey
 */
public class LinkResolverTest extends BaseTest {
    @Inject
    private LinkResolver linkResolver;


    @Override
    public void setup() throws Exception {
        super.setup();
        j.jenkins.getInjector().injectMembers(this);
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


        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/pipeline1/",linkResolver.resolve(pipelineJob1).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/pipeline2/",linkResolver.resolve(pipelineJob2).getHref());

        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/fstyle1/",linkResolver.resolve(f).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/",linkResolver.resolve(folder1).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/test1/",linkResolver.resolve(p1).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/",linkResolver.resolve(folder2).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/test2/",linkResolver.resolve(p2).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/",linkResolver.resolve(folder3).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/pipelines/test3/",linkResolver.resolve(p3).getHref());

    }


    @Test
    public void runLinkResolveTest() throws IOException, ExecutionException, InterruptedException {
        Project f = j.createFreeStyleProject("fstyle1");
        MockFolder folder1 = j.createFolder("folder1");
        Project p1 = folder1.createProject(FreeStyleProject.class, "test1");
        MockFolder folder2 = folder1.createProject(MockFolder.class, "folder2");
        MockFolder folder3 = folder2.createProject(MockFolder.class, "folder3");
        Project p2 = folder2.createProject(FreeStyleProject.class, "test2");
        Project p3 = folder3.createProject(FreeStyleProject.class, "test3");

        Run r = (Run) f.scheduleBuild2(0).get();
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/fstyle1/runs/"+r.getId()+"/",linkResolver.resolve(r).getHref());

        r = (Run) p1.scheduleBuild2(0).get();
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/test1/runs/"+r.getId()+"/",linkResolver.resolve(r).getHref());

        r = (Run) p2.scheduleBuild2(0).get();
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/test2/runs/"+r.getId()+"/",linkResolver.resolve(r).getHref());

        r = (Run) p3.scheduleBuild2(0).get();
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/pipelines/test3/runs/"+r.getId()+"/",linkResolver.resolve(r).getHref());
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
                linkResolver.resolve(nodes.get(0)).getHref());

            Assert.assertEquals(String.format("/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/%s/nodes/%s/",
                b1.getId(),parallelNodes.get(0).getId()),
                linkResolver.resolve(parallelNodes.get(0)).getHref());

            PipelineNodeGraphBuilder graphBuilder = new PipelineNodeGraphBuilder(b1);

            List<FlowNode> steps = graphBuilder.getAllSteps();

            Assert.assertEquals(String.format("/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/%s/steps/%s/",
                b1.getId(),steps.get(0).getId()),
                linkResolver.resolve(steps.get(0)).getHref());

        }
    }
}
