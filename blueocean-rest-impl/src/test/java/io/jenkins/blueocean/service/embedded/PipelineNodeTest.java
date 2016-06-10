package io.jenkins.blueocean.service.embedded;

import hudson.model.Result;
import io.jenkins.blueocean.service.embedded.rest.PipelineNodeUtil;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.visualization.table.FlowGraphTable;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class PipelineNodeTest extends BaseTest {
    @Test
    public void nodesTest() throws Exception {
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

        get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);

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
            "            sh 'sleep 3;'\n" +
            "        }\n" +
            "    },\n" +
            "    \"DelayThenFail\" : {\n" +
            "        node {\n" +
            "            echo 'Fail soon.'\n" +
            "            echo 'KABOOM!'\n" +
            "            sh '11exit 1'\n" +
            "        }\n" +
            "    }\n" +
            "  )\n" +
            "\n" +
            "\n" +
            "stage \"Deploy\"\n" +
            "    node {\n" +
            "        sh \"echo deploying\"\n" +
            "    }"));



        WorkflowRun b2 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE,b2);

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/2/nodes/", List.class);
        Assert.assertEquals(resp.size(), 8);
        for(int i=0; i< resp.size();i++){
            Map rn = resp.get(i);
            List<Map> edges = (List<Map>) rn.get("edges");

            if(rn.get("displayName").equals("Test")){
                Assert.assertEquals(2, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            }else if(rn.get("displayName").equals("Firefox")){
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            }else if(rn.get("displayName").equals("Chrome")){
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            }else if(rn.get("displayName").equals("CrashyMcgee")){
                Assert.assertEquals(2, edges.size());
                Assert.assertEquals(rn.get("result"), "FAILURE");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            }else if(rn.get("displayName").equals("SlowButSuccess")){
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            }else if(rn.get("displayName").equals("DelayThenFail")){
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "FAILURE");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            }else if(rn.get("displayName").equals("build")){
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            }else if(rn.get("displayName").equals("Deploy")){
                Assert.assertEquals(0, edges.size());
                Assert.assertNull(rn.get("result"));
                Assert.assertNull(rn.get("state"));
            }

        }

    }

    @Test
    public void getPipelineJobRunNodesTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");


        job1.setDefinition(new CpsFlowDefinition("stage 'build'\n" +
            "node{\n" +
            "  echo \"Building...\"\n" +
            "}\n" +
            "\n" +
            "stage 'test'\n" +
            "parallel 'unit':{\n" +
            "  node{\n" +
            "    echo \"Unit testing...\"\n" +
            "  }\n" +
            "},'integration':{\n" +
            "  node{\n" +
            "    echo \"Integration testing...\"\n" +
            "  }\n" +
            "}, 'ui':{\n" +
            "  node{\n" +
            "    echo \"UI testing...\"\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "stage 'deploy'\n" +
            "node{\n" +
            "  echo \"Deploying\"\n" +
            "}" +
            "\n" +
            "stage 'deployToProd'\n" +
            "node{\n" +
            "  echo \"Deploying to production\"\n" +
            "}"
        ));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        FlowGraphTable nodeGraphTable = new FlowGraphTable(b1.getExecution());
        nodeGraphTable.build();
        List<FlowNode> nodes = getStages(nodeGraphTable);
        List<FlowNode> parallelNodes = getParallelNodes(nodeGraphTable);

        Assert.assertEquals(7, nodes.size());
        Assert.assertEquals(3, parallelNodes.size());

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);

        Assert.assertEquals(nodes.size(), resp.size());
        for(int i=0; i< nodes.size();i++){
            FlowNode n = nodes.get(i);
            Map rn = resp.get(i);
            Assert.assertEquals(n.getId(), rn.get("id"));
            Assert.assertEquals(getNodeName(n), rn.get("displayName"));
            Assert.assertEquals("SUCCESS", rn.get("result"));
            List<Map> edges = (List<Map>) rn.get("edges");


            Assert.assertTrue((int)rn.get("durationInMillis") > 0);
            if(n.getDisplayName().equals("test")){
                Assert.assertEquals(parallelNodes.size(), edges.size());
                Assert.assertEquals(edges.get(i).get("id"), parallelNodes.get(i).getId());
            }else if(n.getDisplayName().equals("build")){
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(i).get("id"), nodes.get(i+1).getId());
            }else if(n.getDisplayName().equals("deploy")){
                Assert.assertEquals(1, edges.size());
            }else if(n.getDisplayName().equals("deployToProd")){
                Assert.assertEquals(0, edges.size());
            }else{
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(0).get("id"), nodes.get(nodes.size() - 2).getId());
            }
        }
    }


    @Test
    public void getPipelineStepsTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");


        job1.setDefinition(new CpsFlowDefinition("stage 'build'\n" +
            "node{\n" +
            "  sh \"echo Building...\"\n" +
            "}\n" +
            "\n" +
            "stage 'test'\n" +
            "parallel 'unit':{\n" +
            "  node{\n" +
            "    echo \"Unit testing...\"\n" +
            "    sh \"echo Tests running\"\n" +
            "    sh \"echo Tests completed\"\n" +
            "  }\n" +
            "},'integration':{\n" +
            "  node{\n" +
            "    echo \"Integration testing...\"\n" +
            "  }\n" +
            "}, 'ui':{\n" +
            "  node{\n" +
            "    echo \"UI testing...\"\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "node{\n" +
            "  echo \"Done Testing\"\n" +
            "}" +
            "\n" +
            "stage 'deploy'\n" +
            "node{\n" +
            "  echo \"Deploying\"\n" +
            "}" +
            "\n" +
            "stage 'deployToProd'\n" +
            "node{\n" +
            "  echo \"Deploying to production\"\n" +
            "}"
        ));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        FlowGraphTable nodeGraphTable = new FlowGraphTable(b1.getExecution());
        nodeGraphTable.build();
        List<FlowNode> nodes = getStages(nodeGraphTable);
        List<FlowNode> parallelNodes = getParallelNodes(nodeGraphTable);

        Assert.assertEquals(7, nodes.size());
        Assert.assertEquals(3, parallelNodes.size());

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/"+nodes.get(1).getId()+"/steps/", List.class);
        Assert.assertEquals(6, resp.size());

        Map step = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/"+parallelNodes.get(0).getId()+"/steps/"+resp.get(0).get("id"), Map.class);

        Assert.assertNotNull(step);

        String stepLog = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/"+parallelNodes.get(0).getId()+"/steps/"+resp.get(0).get("id")+"/log", String.class);
        Assert.assertNotNull(stepLog);
    }

    @Test
    public void getPipelineJobRunNodesTestWithFuture() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");


        job1.setDefinition(new CpsFlowDefinition("stage 'build'\n" +
            "node{\n" +
            "  echo \"Building...\"\n" +
            "}\n" +
            "\n" +
            "stage 'test'\n" +
            "parallel 'unit':{\n" +
            "  node{\n" +
            "    echo \"Unit testing...\"\n" +
            "  }\n" +
            "},'integration':{\n" +
            "  node{\n" +
            "    echo \"Integration testing...\"\n" +
            "  }\n" +
            "}, 'ui':{\n" +
            "  node{\n" +
            "    echo \"UI testing...\"\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "stage 'deploy'\n" +
            "node{\n" +
            "  echo \"Deploying\"\n" +
            "}" +
            "\n" +
            "stage 'deployToProd'\n" +
            "node{\n" +
            "  echo \"Deploying to production\"\n" +
            "}"
        ));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        FlowGraphTable nodeGraphTable = new FlowGraphTable(b1.getExecution());
        nodeGraphTable.build();
        List<FlowNode> nodes = getStages(nodeGraphTable);
        List<FlowNode> parallelNodes = getParallelNodes(nodeGraphTable);

        Assert.assertEquals(7, nodes.size());
        Assert.assertEquals(3, parallelNodes.size());

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);

        Assert.assertEquals(nodes.size(), resp.size());
        for(int i=0; i< nodes.size();i++){
            FlowNode n = nodes.get(i);
            Map rn = resp.get(i);
            Assert.assertEquals(n.getId(), rn.get("id"));
            Assert.assertEquals(getNodeName(n), rn.get("displayName"));
            Assert.assertEquals("SUCCESS", rn.get("result"));
            List<Map> edges = (List<Map>) rn.get("edges");

            if(n.getDisplayName().equals("test")){
                Assert.assertEquals(parallelNodes.size(), edges.size());
                Assert.assertEquals(edges.get(i).get("id"), parallelNodes.get(i).getId());
            }else if(n.getDisplayName().equals("build")){
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(i).get("id"), nodes.get(i+1).getId());
            }else if(n.getDisplayName().equals("deploy")){
                Assert.assertEquals(1, edges.size());
            }else if(n.getDisplayName().equals("deployToProd")){
                Assert.assertEquals(0, edges.size());
            }else{
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(0).get("id"), nodes.get(nodes.size() - 2).getId());
            }
        }

        job1.setDefinition(new CpsFlowDefinition("stage 'build'\n" +
            "node{\n" +
            "  echo \"Building...\"\n" +
            "}\n" +
            "\n" +
            "stage 'test'\n" +
            "parallel 'unit':{\n" +
            "  node{\n" +
            "    echo \"Unit testing...\"\n" +
            "    sh \"`fail-the-build`\"\n" + //fail the build intentionally
            "  }\n" +
            "},'integration':{\n" +
            "  node{\n" +
            "    echo \"Integration testing...\"\n" +
            "  }\n" +
            "}, 'ui':{\n" +
            "  node{\n" +
            "    echo \"UI testing...\"\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "stage 'deploy'\n" +
            "node{\n" +
            "  echo \"Deploying\"\n" +
            "}" +
            "\n" +
            "stage 'deployToProd'\n" +
            "node{\n" +
            "  echo \"Deploying to production\"\n" +
            "}"
        ));
        b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE,b1);
        resp = get(String.format("/organizations/jenkins/pipelines/pipeline1/runs/%s/nodes/",b1.getId()), List.class);
        Assert.assertEquals(nodes.size(), resp.size());
        for(int i=0; i< nodes.size();i++){
            FlowNode n = nodes.get(i);
            Map rn = resp.get(i);
            Assert.assertEquals(n.getId(), rn.get("id"));
            Assert.assertEquals(getNodeName(n), rn.get("displayName"));
            List<Map> edges = (List<Map>) rn.get("edges");
            if(n.getDisplayName().equals("build")){
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(i).get("id"), nodes.get(i+1).getId());
                Assert.assertEquals("SUCCESS", rn.get("result"));
                Assert.assertEquals("FINISHED", rn.get("state"));
            }else if (n.getDisplayName().equals("test")){
                Assert.assertEquals(parallelNodes.size(), edges.size());
                Assert.assertEquals(edges.get(i).get("id"), parallelNodes.get(i).getId());
                Assert.assertEquals("FAILURE", rn.get("result"));
                Assert.assertEquals("FINISHED", rn.get("state"));
            }else if(PipelineNodeUtil.getDisplayName(n).equals("unit")){
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(0).get("id"), nodes.get(nodes.size() - 2).getId());
                Assert.assertEquals("FAILURE", rn.get("result"));
                Assert.assertEquals("FINISHED", rn.get("state"));
            }else if(n.getDisplayName().equals("deploy")){
                Assert.assertEquals(1, edges.size());
                Assert.assertNull(rn.get("result"));
                Assert.assertNull(rn.get("state"));
                Assert.assertNull(rn.get("startTime"));
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(0).get("id"), nodes.get(nodes.size() - 1).getId());
            }else if(n.getDisplayName().equals("deployToProd")){
                Assert.assertEquals(0, edges.size());
                Assert.assertNull(rn.get("result"));
                Assert.assertNull(rn.get("state"));
                Assert.assertNull(rn.get("startTime"));
                Assert.assertEquals(0, edges.size());
            }else{
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(0).get("id"), nodes.get(nodes.size() - 2).getId());
                Assert.assertEquals("SUCCESS", rn.get("result"));
                Assert.assertEquals("FINISHED", rn.get("state"));
            }
        }

    }

    @Test
    public void getPipelineJobRunNodesWithFailureTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");


        job1.setDefinition(new CpsFlowDefinition("stage 'build'\n" +
            "node{\n" +
            "  echo \"Building...\"\n" +
            "}\n" +
            "\n" +
            "stage 'test'\n" +
            "parallel 'unit':{\n" +
            "  node{\n" +
            "    echo \"Unit testing...\"\n" +
            "    sh \"`fail-the-build`\"\n" + //fail the build intentionally
            "  }\n" +
            "},'integration':{\n" +
            "  node{\n" +
            "    echo \"Integration testing...\"\n" +
            "  }\n" +
            "}, 'ui':{\n" +
            "  node{\n" +
            "    echo \"UI testing...\"\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "stage 'deploy'\n" +
            "node{\n" +
            "  echo \"Deploying\"\n" +
            "}"
        ));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b1);

        FlowGraphTable nodeGraphTable = new FlowGraphTable(b1.getExecution());
        nodeGraphTable.build();
        List<FlowNode> nodes = getStages(nodeGraphTable);
        List<FlowNode> parallelNodes = getParallelNodes(nodeGraphTable);

        Assert.assertEquals(5, nodes.size());
        Assert.assertEquals(3, parallelNodes.size());

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);

        Assert.assertEquals(nodes.size(), resp.size());
        for(int i=0; i< nodes.size();i++){
            FlowNode n = nodes.get(i);
            Map rn = resp.get(i);
            Assert.assertEquals(n.getId(), rn.get("id"));
            Assert.assertEquals(getNodeName(n), rn.get("displayName"));

            List<Map> edges = (List<Map>) rn.get("edges");


            if(n.getDisplayName().equals("test")){
                Assert.assertEquals(parallelNodes.size(), edges.size());
                Assert.assertEquals(edges.get(i).get("id"), parallelNodes.get(i).getId());
                Assert.assertEquals("FAILURE", rn.get("result"));
            }else if(n.getDisplayName().equals("build")){
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(i).get("id"), nodes.get(i+1).getId());
                Assert.assertEquals("SUCCESS", rn.get("result"));
            }else if(n.getDisplayName().equals("Branch: unit")){
                Assert.assertEquals(0, edges.size());
                Assert.assertEquals("FAILURE", rn.get("result"));
            }else{
                Assert.assertEquals(0, edges.size());
                Assert.assertEquals("SUCCESS", rn.get("result"));
            }
        }
    }

    @Test
    public void getPipelineJobRunNodeNoStageTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");


        job1.setDefinition(new CpsFlowDefinition("node{\n" +
            "  parallel 'unit':{\n" +
            "    node{\n" +
            "      echo \"Unit testing...\"\n" +
            "    }\n" +
            "  },'integration':{\n" +
            "    node{\n" +
            "      echo \"Integration testing...\"\n" +
            "    }\n" +
            "  }, 'ui':{\n" +
            "    node{\n" +
            "      echo \"UI testing...\"\n" +
            "    }\n" +
            "  }\n" +
            "}"));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);
        FlowGraphTable nodeGraphTable = new FlowGraphTable(b1.getExecution());
        nodeGraphTable.build();
        List<FlowNode> nodes = getStages(nodeGraphTable);
        List<FlowNode> parallelNodes = getParallelNodes(nodeGraphTable);

        Assert.assertEquals(3, nodes.size());
        Assert.assertEquals(3, parallelNodes.size());

        // get all nodes for pipeline1
        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(nodes.size(), resp.size());

    }


    @Test
    public void getPipelineJobRunNodeTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");


        job1.setDefinition(new CpsFlowDefinition("stage 'build'\n" +
            "node{\n" +
            "  echo \"Building...\"\n" +
            "}\n" +
            "\n" +
            "stage 'test'\n" +
            "parallel 'unit':{\n" +
            "  node{\n" +
            "    echo \"Unit testing...\"\n" +
            "  }\n" +
            "},'integration':{\n" +
            "  node{\n" +
            "    echo \"Integration testing...\"\n" +
            "  }\n" +
            "}, 'ui':{\n" +
            "  node{\n" +
            "    echo \"UI testing...\"\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "stage 'deploy'\n" +
            "node{\n" +
            "  echo \"Deploying\"\n" +
            "}"));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);
        FlowGraphTable nodeGraphTable = new FlowGraphTable(b1.getExecution());
        nodeGraphTable.build();
        List<FlowNode> nodes = getStages(nodeGraphTable);
        List<FlowNode> parallelNodes = getParallelNodes(nodeGraphTable);

        Assert.assertEquals(6, nodes.size());
        Assert.assertEquals(3, parallelNodes.size());

        // get all nodes for pipeline1
        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(nodes.size(), resp.size());

        //Get a node detail
        FlowNode n = nodes.get(0);

        Map node = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/"+n.getId());

        List<Map> edges = (List<Map>) node.get("edges");

        Assert.assertEquals(n.getId(), node.get("id"));
        Assert.assertEquals(getNodeName(n), node.get("displayName"));
        Assert.assertEquals("SUCCESS", node.get("result"));
        Assert.assertEquals(1, edges.size());
        Assert.assertEquals(nodes.get(1).getId(), edges.get(0).get("id"));


        //Get a parllel node detail
        node = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/"+parallelNodes.get(0).getId());

        n = parallelNodes.get(0);
        edges = (List<Map>) node.get("edges");

        Assert.assertEquals(n.getId(), node.get("id"));
        Assert.assertEquals(getNodeName(n), node.get("displayName"));
        Assert.assertEquals("SUCCESS", node.get("result"));
        Assert.assertEquals(1, edges.size());
        Assert.assertEquals(nodes.get(nodes.size()-1).getId(), edges.get(0).get("id"));
    }


    @Test
    public void getPipelineJobRunNodeLogTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");


        job1.setDefinition(new CpsFlowDefinition("stage 'build'\n" +
            "node{\n" +
            "  echo \"Building...\"\n" +
            "}\n" +
            "\n" +
            "stage 'test'\n" +
            "parallel 'unit':{\n" +
            "  node{\n" +
            "    echo \"Unit testing...\"\n" +
            "  }\n" +
            "},'integration':{\n" +
            "  node{\n" +
            "    echo \"Integration testing...\"\n" +
            "  }\n" +
            "}, 'ui':{\n" +
            "  node{\n" +
            "    echo \"UI testing...\"\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "stage 'deploy'\n" +
            "node{\n" +
            "  echo \"Deploying\"\n" +
            "}"));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);
        FlowGraphTable nodeGraphTable = new FlowGraphTable(b1.getExecution());
        nodeGraphTable.build();
        List<FlowNode> nodes = getStages(nodeGraphTable);
        List<FlowNode> parallelNodes = getParallelNodes(nodeGraphTable);

        Assert.assertEquals(6, nodes.size());
        Assert.assertEquals(3, parallelNodes.size());

        String output = get("/organizations/jenkins/pipelines/pipeline1/runs/1/log", String.class);
        Assert.assertNotNull(output);
        System.out.println(output);
    }

    private List<FlowNode> getStages(FlowGraphTable nodeGraphTable){
        List<FlowNode> nodes = new ArrayList<>();
        for(FlowGraphTable.Row row: nodeGraphTable.getRows()){
            if(PipelineNodeUtil.isStage(row.getNode()) ||
                PipelineNodeUtil.isParallelBranch(row.getNode())){
                nodes.add(row.getNode());
            }
        }
        return nodes;
    }

    private List<FlowNode> getParallelNodes(FlowGraphTable nodeGraphTable){
        List<FlowNode> parallelNodes = new ArrayList<>();

        for(FlowGraphTable.Row row: nodeGraphTable.getRows()){
            if(PipelineNodeUtil.isParallelBranch(row.getNode())){
                parallelNodes.add(row.getNode());
            }
        }
        return parallelNodes;
    }

}
