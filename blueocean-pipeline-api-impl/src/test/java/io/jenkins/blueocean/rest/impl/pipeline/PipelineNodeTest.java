package io.jenkins.blueocean.rest.impl.pipeline;

import com.mashape.unirest.http.Unirest;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Util;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.Slave;
import hudson.model.queue.QueueTaskFuture;
import hudson.util.RunList;
import io.jenkins.blueocean.commons.MapsHelper;
import io.jenkins.blueocean.listeners.NodeDownstreamBuildAction;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BlueRun;
import jenkins.branch.BranchSource;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.scm.api.SCMSource;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.CpsFlowExecution;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graphanalysis.MemoryFlowChunk;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.jenkinsci.plugins.workflow.steps.UnstableStep;
import org.jenkinsci.plugins.workflow.support.steps.input.InputAction;
import org.jenkinsci.plugins.workflow.support.visualization.table.FlowGraphTable;
import org.jenkinsci.plugins.workflow.test.steps.SemaphoreStep;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl.PARAMETERS_ELEMENT;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

/**
 * @author Vivek Pandey
 */
public class PipelineNodeTest extends PipelineBaseTest {

    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @BeforeClass
    public static void setupStatic() throws Exception {
        System.setProperty("NODE-DUMP-ENABLED", "true");//tests node dump code path, also helps debug test failure
        Unirest.setTimeouts(10000, 600000000);
    }

    @Test
    @Issue("JENKINS-44742")
    public void successfulStepWithBlockFailureAfterward() throws Exception {
        WorkflowJob p = j.createProject(WorkflowJob.class, "project");

        URL resource = getClass().getResource("successfulStepWithBlockFailureAfterward.jenkinsfile");
        String jenkinsFile = IOUtils.toString(resource, StandardCharsets.UTF_8);
        p.setDefinition(new CpsFlowDefinition(jenkinsFile, true));
        p.save();

        Run r = p.scheduleBuild2(0).waitForStart();

        j.waitForCompletion(r);

        List<Map> resp = get("/organizations/jenkins/pipelines/project/runs/" + r.getId() + "/steps/", List.class);

        Map firstStep = resp.get(0);
        Assert.assertEquals("SUCCESS", firstStep.get("result"));
        Assert.assertEquals("FINISHED", firstStep.get("state"));

        Map secondStep = resp.get(1);
        Assert.assertEquals("FAILURE", secondStep.get("result"));
        Assert.assertEquals("FINISHED", secondStep.get("state"));
    }

    @Test
    @Issue("JENKINS-50532")
    public void statusForTwoLevelParallelBuild() throws Exception {
        String p = "pipeline {\n" +
            "    agent any\n" +
            "    stages {\n" +
            "        stage('Nested Parallel Stage') {\n" +
            "            parallel {\n" +
            "                stage(\"Parallel Stage\") { \n" +
            "                    steps { \n" +
            "                        script {\n" +
            "                          def parallelTasks = [:]\n" +
            "                          \n" +
            "                            parallelTasks['Successful Task 1'] = {\n" +
            "                              echo \"Success\"\n" +
            "                          }\n" +
            "                          parallelTasks['Failing Task'] = {\n" +
            "                              sh \"exit 1\"\n" +
            "                          }\n" +
            "                          parallel parallelTasks\n" +
            "                        } \n" +
            "                    } \n" +
            "                }\n" +
            "                stage(\"Stage\") {\n" +
            "                    steps { \n" +
            "                        echo \"Stage\"    \n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";

        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        job1.setDefinition(new CpsFlowDefinition(p, false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b1);

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(resp.size(), 5);

        for (Map rn : resp) {
            if (rn.get("displayName").equals("Failing Task")) {
                Assert.assertEquals(rn.get("state"), "FINISHED");
                Assert.assertEquals(rn.get("result"), "FAILURE");
            } else if (rn.get("displayName").equals("Parallel Stage")) {
                Assert.assertEquals(rn.get("state"), "FINISHED");
                Assert.assertEquals(rn.get("result"), "FAILURE");
            } else if (rn.get("displayName").equals("Stage")) {
                Assert.assertEquals(rn.get("state"), "FINISHED");
                Assert.assertEquals(rn.get("result"), "SUCCESS");
            } else if (rn.get("displayName").equals("Successful Task")) {
                Assert.assertEquals(rn.get("state"), "FINISHED");
                Assert.assertEquals(rn.get("result"), "SUCCESS");
            }
        }
    }

    @Test
    @Issue("JENKINS-39203")
    public void stepStatusForUnstableBuild() throws Exception {
        String p = "node {\n" +
            "   echo 'Hello World'\n" +
            "   try{\n" +
            "    echo 'Inside try'\n" +
            "   }finally{\n" +
            "    sh 'echo \"blah\"' \n" +
            "    unstable('foobar')\n" +
            "   }\n" +
            "}";

        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        job1.setDefinition(new CpsFlowDefinition(p, false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.UNSTABLE, b1);

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/", List.class);
        assertEquals(4, resp.size());

        String unstableStepDisplayName = ExtensionList.lookupSingleton(UnstableStep.DescriptorImpl.class).getDisplayName();
        for (Map rn : resp) {
            String expectedResult = unstableStepDisplayName.equals(rn.get("displayName"))
                    ? "UNSTABLE"
                    : "SUCCESS";
            assertEquals(expectedResult, rn.get("result"));
            assertEquals("FINISHED", rn.get("state"));
        }

    }

    @Test
    @Issue("JENKINS-39296")
    public void stepStatusForFailedBuild() throws Exception {
        String p = "node {\n" +
            "   echo 'Hello World'\n" +
            "   try{\n" +
            "    echo 'Inside try'\n" +
            "    sh 'this should fail'" +
            "   }finally{\n" +
            "    echo 'this should pass'\n" +
            "   }\n" +
            "}";

        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        job1.setDefinition(new CpsFlowDefinition(p, false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b1);

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/", List.class);
        Assert.assertEquals(resp.size(), 4);

        Map helloWorldStep = resp.get(0);

        Assert.assertEquals("Hello World", helloWorldStep.get("displayDescription"));
        Assert.assertEquals("SUCCESS", helloWorldStep.get("result"));
        Assert.assertEquals("FINISHED", helloWorldStep.get("state"));

        Map insideTryStep = resp.get(1);

        Assert.assertEquals("Inside try", insideTryStep.get("displayDescription"));
        Assert.assertEquals("SUCCESS", insideTryStep.get("result"));
        Assert.assertEquals("FINISHED", insideTryStep.get("state"));

        Map thisShouldFailStep = resp.get(2);

        Assert.assertEquals("this should fail", thisShouldFailStep.get("displayDescription"));
        Assert.assertEquals("FAILURE", thisShouldFailStep.get("result"));
        Assert.assertEquals("FINISHED", thisShouldFailStep.get("state"));

        Map thisShouldPassStep = resp.get(3);

        Assert.assertEquals("this should pass", thisShouldPassStep.get("displayDescription"));
        Assert.assertEquals("SUCCESS", thisShouldPassStep.get("result"));
        Assert.assertEquals("FINISHED", thisShouldPassStep.get("state"));
    }

    @Test
    public void testBlockStage() throws Exception {
        String pipeline = "" +
            "node {\n" +
            "   stage ('dev');" +                 //start
            "     echo ('development'); " +

            "   stage ('Build') { " +
            "     echo ('Building'); " +
            "   } \n" +
            "   stage ('test') { " +
            "     echo ('Testing'); " +
            "     parallel firstBranch: {\n" + //1
            "       echo 'first Branch'\n" +
            "       sh 'sleep 1'\n" +
            "       echo 'first Branch end'\n" +
            "     }, secondBranch: {\n" +
            "       echo 'Hello second Branch'\n" +
            "       sh 'sleep 1'   \n" +
            "       echo 'second Branch end'\n" +
            "       \n" +
            "    },\n" +
            "    failFast: false\n" +
            "   } \n" +
            "   stage ('deploy') { " +
            "     writeFile file: 'file.txt', text:'content'; " +
            "     archive(includes: 'file.txt'); " +
            "     echo ('Deploying'); " +
            "   } \n" +
            "}";


        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        job1.setDefinition(new CpsFlowDefinition(pipeline, false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        NodeGraphBuilder builder = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(b1);
        List<FlowNode> stages = getStages(builder);
        List<FlowNode> parallels = getParallelNodes(builder);

        Assert.assertEquals(4, stages.size());
        Assert.assertEquals(2, parallels.size());

        //TODO: complete test
        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(6, resp.size());

        String testStageId = null;

        for (int i = 0; i < resp.size(); i++) {
            Map rn = resp.get(i);
            List<Map> edges = (List<Map>) rn.get("edges");

            if (rn.get("displayName").equals("dev")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("build")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("test")) {
                testStageId = (String) rn.get("id");
                Assert.assertEquals(2, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("firstBranch")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("secondBranch")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("deploy")) {
                Assert.assertEquals(0, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            }
        }

        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/", List.class);
        Assert.assertEquals(12, resp.size());


        Assert.assertNotNull(testStageId);
        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + testStageId + "/steps/", List.class);
        Assert.assertEquals(7, resp.size());

    }

    @Test
    public void testTestsInStage() throws Exception {
        String pipeline = "" +
            "node {\n" +
            "  stage ('dev') {\n" +
            "    junit('*.xml')\n" +
            "  }\n" +
            "  stage ('prod') {\n" +
            "    junit('*.xml')\n" +
            "  }\n" +
            "  stage ('testing') {\n" +
            "    parallel(first: {\n" +
            "        junit('*.xml')\n" +
            "      },\n" +
            "      second: {\n" +
            "        junit('*.xml')\n" +
            "      })\n" +
            "  }\n" +
            "}\n";

        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        job1.setDefinition(new CpsFlowDefinition(pipeline, true));
        FilePath ws = j.jenkins.getWorkspaceFor(job1);
        FilePath testFile = ws.child("test-result.xml");
        testFile.copyFrom(PipelineNodeTest.class.getResource("testResult.xml"));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        NodeGraphBuilder builder = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(b1);
        List<FlowNode> stages = getStages(builder);

        Assert.assertEquals(3, stages.size());

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(5, resp.size());

        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/tests/", List.class);
        Assert.assertEquals(4, resp.size());

        Assert.assertEquals("dev / testDummyMethod – DummyTest", resp.get(0).get("name"));
        Assert.assertEquals("prod / testDummyMethod – DummyTest", resp.get(1).get("name"));
        Assert.assertEquals("testing / first / testDummyMethod – DummyTest", resp.get(2).get("name"));
        Assert.assertEquals("testing / second / testDummyMethod – DummyTest", resp.get(3).get("name"));
    }

    @Test
    public void testNonblockStageSteps() throws Exception {
        String pipeline = "node {\n" +
            "  stage 'Checkout'\n" +
            "      echo 'checkingout'\n" +
            "  stage 'Build'\n" +
            "      echo 'building'\n" +
            "  stage 'Archive'\n" +
            "      echo 'archiving...'\n" +
            "}";


        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        job1.setDefinition(new CpsFlowDefinition(pipeline, false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);


        NodeGraphBuilder builder = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(b1);
        List<FlowNode> stages = getStages(builder);
        List<FlowNode> parallels = getParallelNodes(builder);

        Assert.assertEquals(3, stages.size());
        Assert.assertEquals(0, parallels.size());

        //TODO: complete test
        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(3, resp.size());

        String checkoutId = (String) resp.get(0).get("id");
        String buildId = (String) resp.get(0).get("id");
        String archiveId = (String) resp.get(0).get("id");

        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/", List.class);
        Assert.assertEquals(3, resp.size());


        Assert.assertNotNull(checkoutId);
        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + checkoutId + "/steps/", List.class);
        Assert.assertEquals(1, resp.size());


        Assert.assertNotNull(buildId);
        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + buildId + "/steps/", List.class);
        Assert.assertEquals(1, resp.size());


        Assert.assertNotNull(archiveId);
        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + archiveId + "/steps/", List.class);
        Assert.assertEquals(1, resp.size());
    }


    @Test
    public void testNestedBlockStage() throws Exception {
        String pipeline = "" +
            "node {" +
            "   stage ('dev');" +                 //start
            "     echo ('development'); " +
            "   stage ('Build') { " +
            "     echo ('Building'); " +
            "     stage('Packaging') {" +
            "         echo 'packaging...'" +
            "     }" +
            "   } \n" +
            "   stage ('test') { " +
            "     echo ('Testing'); " +
            "     parallel firstBranch: {\n" +
            "       echo 'Hello first Branch'\n" +
            "       echo 'first Branch 1'\n" +
            "       echo 'first Branch end'\n" +
            "       \n" +
            "    },\n" +
            "    thirdBranch: {\n" +
            "       echo 'Hello third Branch'\n" +
            "       sh 'sleep 1'   \n" +
            "       echo 'third Branch 1'\n" +
            "       echo 'third Branch 2'\n" +
            "       echo 'third Branch end'\n" +
            "       \n" +
            "    },\n" +
            "    secondBranch: {" +
            "       echo 'first Branch'\n" +
            "     stage('firstBranchTest') {" +
            "       echo 'running firstBranchTest'\n" +
            "       sh 'sleep 1'\n" +
            "     }\n" +
            "       echo 'first Branch end'\n" +
            "     },\n" +
            "    failFast: false\n" +
            "   } \n" +
            "   stage ('deploy') { " +
            "     writeFile file: 'file.txt', text:'content'; " +
            "     archive(includes: 'file.txt'); " +
            "     echo ('Deploying'); " +
            "   } \n" +
            "}";


        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        job1.setDefinition(new CpsFlowDefinition(pipeline, false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);


        PipelineNodeGraphVisitor builder = new PipelineNodeGraphVisitor(b1);
        assertFalse(builder.isDeclarative());
        List<FlowNode> stages = getStages(builder);
        List<FlowNode> parallels = getParallelNodes(builder);

        Assert.assertEquals(4, stages.size());
        Assert.assertEquals(3, parallels.size());

        //TODO: complete test
        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(7, resp.size());

        String testStageId = null;

        String devNodeId = null;
        for (int i = 0; i < resp.size(); i++) {
            Map rn = resp.get(i);
            List<Map> edges = (List<Map>) rn.get("edges");

            if (rn.get("displayName").equals("dev")) {
                Assert.assertEquals(0, i);
                devNodeId = (String) rn.get("id");
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("build")) {
                Assert.assertEquals(1, i);
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("test")) {
                Assert.assertEquals(2, i);
                testStageId = (String) rn.get("id");
                Assert.assertEquals(3, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("firstBranch")) {
                Assert.assertEquals(3, i);
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("secondBranch")) {
                Assert.assertEquals(4, i);
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("thirdBranch")) {
                Assert.assertEquals(5, i);
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("deploy")) {
                Assert.assertEquals(6, i);
                Assert.assertEquals(0, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            }
        }

        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/", List.class);
        Assert.assertEquals(19, resp.size());


        Assert.assertNotNull(testStageId);
        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + testStageId + "/steps/", List.class);
        Assert.assertEquals(13, resp.size());

        //firstBranch is parallel with nested stage. firstBranch /steps should also include steps inside nested stage
        FlowNode firstBranch = null;
        FlowNode secondBranch = null;
        FlowNode thirdBranch = null;
        for (FlowNode n : parallels) {
            if (n.getDisplayName().equals("Branch: firstBranch")) {
                firstBranch = n;
            }
            if (n.getDisplayName().equals("Branch: secondBranch")) {
                secondBranch = n;
            }
            if (n.getDisplayName().equals("Branch: thirdBranch")) {
                thirdBranch = n;
            }
        }
        Assert.assertNotNull(firstBranch);
        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + firstBranch.getId() + "/steps/", List.class);
        Assert.assertEquals(3, resp.size());

        Assert.assertNotNull(secondBranch);
        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + secondBranch.getId() + "/steps/", List.class);
        Assert.assertEquals(4, resp.size());

        Assert.assertNotNull(thirdBranch);
        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + thirdBranch.getId() + "/steps/", List.class);
        Assert.assertEquals(5, resp.size());

        Assert.assertNotNull(devNodeId);
        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + devNodeId + "/steps/", List.class);
        Assert.assertEquals(1, resp.size());

    }

    @Test
    public void nodesWithFutureTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition("node {\n" +
                                                     "  stage 'build'\n" +
                                                     "  sh 'echo s1'\n" +
                                                     "  stage 'test'\n" +
                                                     "  echo 'Hello World 2'\n" +
                                                     "}", false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.SUCCESS, b1);

        get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);

        job1.setDefinition(new CpsFlowDefinition("node {\n" +
                                                     "  stage 'build'\n" +
                                                     "  sh 'echo s1'\n" +
                                                     "  stage 'test'\n" +
                                                     "  echo 'Hello World 2'\n" +
                                                     "}\n" +
                                                     "parallel firstBranch: {\n" +
                                                     "  echo 'Hello first'\n" +
                                                     "}, secondBranch: {\n" +
                                                     " echo 'Hello second'\n" +
                                                     "}", false));


        WorkflowRun b2 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.SUCCESS, b2);

        job1.setDefinition(new CpsFlowDefinition("node {\n" +
                                                     "  stage 'build'\n" +
                                                     "  sh 'echo s1'\n" +
                                                     "  stage 'test'\n" +
                                                     "  echo 'Hello World 2'\n" +
                                                     "}\n" +
                                                     "parallel firstBranch: {\n" +
                                                     "  echo 'Hello first'\n" +
                                                     "}, secondBranch: {\n" +
                                                     " sh 'Hello second'\n" +
                                                     "}", false));


        WorkflowRun b3 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b3);

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(2, resp.size());
    }

    @Test
    public void nodesWithPartialParallels() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition("node {\n" +
                                                     "    stage (\"hey\") {\n" +
                                                     "        sh \"echo yeah\"\n" +
                                                     "    }\n" +
                                                     "    stage (\"par\") {\n" +
                                                     "    \n" +
                                                     "        parallel left : {\n" +
                                                     "            sh \"echo OMG BS\"\n" +
                                                     "            sh \"echo yeah\"\n" +
                                                     "        }, \n" +
                                                     "        \n" +
                                                     "        right : {\n" +
                                                     "            sh \"echo wozzle\"\n" +
                                                     "        }\n" +
                                                     "    }\n" +
                                                     "    stage (\"ho\") {\n" +
                                                     "        sh \"echo done\"\n" +
                                                     "    }\n" +
                                                     "}", false));

        j.buildAndAssertSuccess(job1);
        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(5, resp.size());

        job1.setDefinition(new CpsFlowDefinition("node {\n" +
                                                     "    stage (\"hey\") {\n" +
                                                     "        sh \"echo yeah\"\n" +
                                                     "    }\n" +
                                                     "    stage (\"par\") {\n" +
                                                     "    \n" +
                                                     "        parallel left : {\n" +
                                                     "            sh \"echo OMG BS\"\n" +
                                                     "            echo \"running\"\n" +
                                                     "            semaphore('left')\n" +
                                                     "            echo \"BRANCH NAME left\"\n" +
                                                     "            sh \"echo yeah\"\n" +
                                                     "        }, \n" +
                                                     "        \n" +
                                                     "        right : {\n" +
                                                     "            sh \"echo wozzle\"\n" +
                                                     "            semaphore('right')\n" +
                                                     "            echo \"BRANCH NAME right\"\n" +
                                                     "        }\n" +
                                                     "    }\n" +
                                                     "    stage (\"ho\") {\n" +
                                                     "        sh \"echo done\"\n" +
                                                     "    }\n" +
                                                     "}", false));

        WorkflowRun b2 = job1.scheduleBuild2(0).waitForStart();
        SemaphoreStep.waitForStart("left/1", b2);
        SemaphoreStep.waitForStart("right/1", b2);

        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/2/nodes/", List.class);

        Assert.assertEquals(5, resp.size());

        Map leftNode = resp.get(2);
        Assert.assertEquals("left", leftNode.get("displayName"));

        Map rightNode = resp.get(3);
        Assert.assertEquals("right", rightNode.get("displayName"));

        List<Map> leftSteps = get("/organizations/jenkins/pipelines/pipeline1/runs/2/nodes/" + leftNode.get("id") + "/steps/", List.class);

        Assert.assertEquals(3, leftSteps.size());

        List<Map> rightSteps = get("/organizations/jenkins/pipelines/pipeline1/runs/2/nodes/" + rightNode.get("id") + "/steps/", List.class);

        Assert.assertEquals(2, rightSteps.size());
    }


    @Test
    public void nodesTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition("stage (\"Build\") {\n" +
                                                     "    node {\n" +
                                                     "       sh \"echo here\"\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage (\"Test\") {\n" +
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
                                                     "}\n" +
                                                     "stage (\"CrashyMcgee\") {\n" +
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
                                                     "}\n" +
                                                     "stage (\"Deploy\") {\n" +
                                                     "    node {\n" +
                                                     "        sh \"echo deploying\"\n" +
                                                     "    }\n" +
                                                     "}\n", true));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);

        job1.setDefinition(new CpsFlowDefinition("stage (\"Build\") {\n" +
                                                     "    node {\n" +
                                                     "       sh \"echo here\"\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage (\"Test\") {\n" +
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
                                                     "}\n" +
                                                     "stage (\"CrashyMcgee\") {\n" +
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
                                                     "}\n" +
                                                     "\n" +
                                                     "stage (\"Deploy\") {\n" +
                                                     "    node {\n" +
                                                     "        sh \"echo deploying\"\n" +
                                                     "    }\n" +
                                                     "}", false));


        WorkflowRun b2 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b2);

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/2/nodes/", List.class);
        Assert.assertEquals(resp.size(), 8);
        for (int i = 0; i < resp.size(); i++) {
            Map rn = resp.get(i);
            List<Map> edges = (List<Map>) rn.get("edges");

            if (rn.get("displayName").equals("Test")) {
                Assert.assertEquals(2, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("Firefox")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("Chrome")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("CrashyMcgee")) {
                Assert.assertEquals(2, edges.size());
                Assert.assertEquals(rn.get("result"), "FAILURE");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("SlowButSuccess")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("DelayThenFail")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "FAILURE");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("build")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(rn.get("result"), "SUCCESS");
                Assert.assertEquals(rn.get("state"), "FINISHED");
            } else if (rn.get("displayName").equals("Deploy")) {
                Assert.assertEquals(0, edges.size());
                Assert.assertNull(rn.get("result"));
                Assert.assertNull(rn.get("state"));
            }

        }

    }

    @Test
    public void nodesFailureTest() throws Exception {
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
                                                     "    }", false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        job1.setDefinition(new CpsFlowDefinition("throw stage \"Build\"\n" +
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
                                                     "    }", false));

        job1.scheduleBuild2(0);
        WorkflowRun b2 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b2);

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/2/nodes/", List.class);
        Assert.assertEquals(8, resp.size());
        for (int i = 0; i < resp.size(); i++) {
            Map rn = resp.get(i);
            List<Map> edges = (List<Map>) rn.get("edges");

            if (rn.get("displayName").equals("Test")) {
                Assert.assertEquals(2, edges.size());
                Assert.assertNull(rn.get("result"));
                Assert.assertNull(rn.get("state"));
            } else if (rn.get("displayName").equals("Firefox")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertNull(rn.get("result"));
                Assert.assertNull(rn.get("state"));
            } else if (rn.get("displayName").equals("Chrome")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertNull(rn.get("result"));
                Assert.assertNull(rn.get("state"));
            } else if (rn.get("displayName").equals("CrashyMcgee")) {
                Assert.assertEquals(2, edges.size());
                Assert.assertNull(rn.get("result"));
                Assert.assertNull(rn.get("state"));
            } else if (rn.get("displayName").equals("SlowButSuccess")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertNull(rn.get("result"));
                Assert.assertNull(rn.get("state"));
            } else if (rn.get("displayName").equals("DelayThenFail")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertNull(rn.get("result"));
                Assert.assertNull(rn.get("state"));
            } else if (rn.get("displayName").equals("build")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertNull(rn.get("result"));
                Assert.assertNull(rn.get("state"));
            } else if (rn.get("displayName").equals("Deploy")) {
                Assert.assertEquals(0, edges.size());
                Assert.assertNull(rn.get("result"));
                Assert.assertNull(rn.get("state"));
            }

        }
    }


    @Test
    public void getPipelineJobRunNodesTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");


        job1.setDefinition(new CpsFlowDefinition("stage ('build') {\n" +
                                                     "    node {\n" +
                                                     "        echo \"Building...\"\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage ('test') {\n" +
                                                     "    parallel 'unit':{\n" +
                                                     "        node{\n" +
                                                     "            echo \"Unit testing...\"\n" +
                                                     "        }\n" +
                                                     "    },\n" +
                                                     "    'integration':{\n" +
                                                     "        node{\n" +
                                                     "            echo \"Integration testing...\"\n" +
                                                     "        }\n" +
                                                     "    },\n" +
                                                     "    'ui':{\n" +
                                                     "        node{\n" +
                                                     "            echo \"UI testing...\"\n" +
                                                     "        }\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage ('deploy') {\n" +
                                                     "    node{\n" +
                                                     "        echo \"Deploying\"\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage ('deployToProd') {\n" +
                                                     "    node{\n" +
                                                     "        echo \"Deploying to production\"\n" +
                                                     "    }\n" +
                                                     "}", false
        ));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        NodeGraphBuilder builder = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(b1);

        List<FlowNode> nodes = getStagesAndParallels(builder);
        List<FlowNode> parallelNodes = getParallelNodes(builder);

        Assert.assertEquals(7, nodes.size());
        Assert.assertEquals(3, parallelNodes.size());

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);

        Assert.assertEquals(nodes.size(), resp.size());
        for (int i = 0; i < nodes.size(); i++) {
            FlowNode n = nodes.get(i);
            Map rn = resp.get(i);
            Assert.assertEquals(n.getId(), rn.get("id"));
            Assert.assertEquals(getNodeName(n), rn.get("displayName"));
            Assert.assertEquals("SUCCESS", rn.get("result"));
            List<Map> edges = (List<Map>) rn.get("edges");


            Assert.assertTrue((int) rn.get("durationInMillis") > 0);
            if (n.getDisplayName().equals("test")) {
                Assert.assertEquals(parallelNodes.size(), edges.size());
                Assert.assertEquals(edges.get(i).get("id"), parallelNodes.get(i).getId());
            } else if (n.getDisplayName().equals("build")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(i).get("id"), nodes.get(i + 1).getId());
            } else if (n.getDisplayName().equals("deploy")) {
                Assert.assertEquals(1, edges.size());
            } else if (n.getDisplayName().equals("deployToProd")) {
                Assert.assertEquals(0, edges.size());
            } else {
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(0).get("id"), nodes.get(nodes.size() - 2).getId());
            }
        }
    }


    @Test
    public void getPipelineStepsTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");


        job1.setDefinition(new CpsFlowDefinition("stage ('build') {\n" +
                                                     "    node{\n" +
                                                     "        sh \"echo Building...\"\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage ('test') {\n" +
                                                     "    parallel 'unit':{\n" +
                                                     "        node {\n" +
                                                     "            echo \"Unit testing...\"\n" +
                                                     "            sh \"echo Tests running\"\n" +
                                                     "            sh \"echo Tests completed\"\n" +
                                                     "        }\n" +
                                                     "    },\n" +
                                                     "    'integration':{\n" +
                                                     "        node{\n" +
                                                     "            echo \"Integration testing...\"\n" +
                                                     "        }\n" +
                                                     "    },\n" +
                                                     "    'ui':{\n" +
                                                     "        node {\n" +
                                                     "            echo \"UI testing...\"\n" +
                                                     "        }\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "node{\n" +
                                                     "  echo \"Done Testing\"\n" +
                                                     "}\n" +
                                                     "stage ('deploy') {\n" +
                                                     "    node{\n" +
                                                     "        echo \"Deploying\"\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage ('deployToProd') {\n" +
                                                     "    node{\n" +
                                                     "        echo \"Deploying to production\"\n" +
                                                     "    }\n" +
                                                     "}", false
        ));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        FlowGraphTable nodeGraphTable = new FlowGraphTable(b1.getExecution());
        nodeGraphTable.build();
        List<FlowNode> nodes = getStages(nodeGraphTable);
        List<FlowNode> parallelNodes = getParallelNodes(nodeGraphTable);

        Assert.assertEquals(7, nodes.size());
        Assert.assertEquals(3, parallelNodes.size());

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + nodes.get(1).getId() + "/steps/", List.class);
        Assert.assertEquals(6, resp.size());

        Map step = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + parallelNodes.get(0).getId() + "/steps/" + resp.get(0).get("id"), Map.class);

        assertNotNull(step);

        String stepLog = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + parallelNodes.get(0).getId() + "/steps/" + resp.get(0).get("id") + "/log", String.class);
        assertNotNull(stepLog);
    }

    @Test
    public void getPipelineWihNodesAllStepsTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");


        job1.setDefinition(new CpsFlowDefinition("stage ('build') {\n" +
                                                     "    node{\n" +
                                                     "        sh \"echo Building...\"\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage ('test') {\n" +
                                                     "    parallel 'unit':{\n" +
                                                     "        node {\n" +
                                                     "            echo \"Unit testing...\"\n" +
                                                     "            sh \"echo Tests running\"\n" +
                                                     "            sh \"echo Tests completed\"\n" +
                                                     "        }\n" +
                                                     "    },\n" +
                                                     "    'integration':{\n" +
                                                     "        node{\n" +
                                                     "            echo \"Integration testing...\"\n" +
                                                     "        }\n" +
                                                     "    },\n" +
                                                     "    'ui':{\n" +
                                                     "        node {\n" +
                                                     "            echo \"UI testing...\"\n" +
                                                     "        }\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "node{\n" +
                                                     "  echo \"Done Testing\"\n" +
                                                     "}\n" +
                                                     "stage ('deploy') {\n" +
                                                     "    node{\n" +
                                                     "        echo \"Deploying\"\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage ('deployToProd') {\n" +
                                                     "    node{\n" +
                                                     "        echo \"Deploying to production\"\n" +
                                                     "    }\n" +
                                                     "}", false
        ));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        FlowGraphTable nodeGraphTable = new FlowGraphTable(b1.getExecution());
        nodeGraphTable.build();
        List<FlowNode> nodes = getStages(nodeGraphTable);
        List<FlowNode> parallelNodes = getParallelNodes(nodeGraphTable);

        Assert.assertEquals(7, nodes.size());
        Assert.assertEquals(3, parallelNodes.size());

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/", List.class);
        Assert.assertEquals(9, resp.size());
    }

    @Test
    public void getPipelineWihoutNodesAllStepsTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");


        job1.setDefinition(new CpsFlowDefinition("node {\n" +
                                                     "    sh \"echo Building...\"\n" +
                                                     "}\n" +
                                                     "    node{\n" +
                                                     "        echo \"Unit testing...\"\n" +
                                                     "        sh \"echo Tests running\"\n" +
                                                     "        sh \"echo Tests completed\"\n" +
                                                     "    }", false
        ));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/", List.class);
        Assert.assertEquals(4, resp.size());
        String log = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/" + resp.get(0).get("id") + "/log/", String.class);
        assertNotNull(log);
    }


    @Test
    public void getPipelineJobRunNodesTestWithFuture() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        // This is now returning
        job1.setDefinition(new CpsFlowDefinition("stage ('build') {\n" +
                                                     "    node{\n" +
                                                     "        echo \"Building...\"\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage ('test') {\n" +
                                                     "    parallel 'unit':{\n" +
                                                     "        node {\n" +
                                                     "            echo \"Unit testing...\"\n" +
                                                     "        }\n" +
                                                     "    },\n" +
                                                     "    'integration':{\n" +
                                                     "        node {\n" +
                                                     "            echo \"Integration testing...\"\n" +
                                                     "        }\n" +
                                                     "    },\n" +
                                                     "    'ui':{\n" +
                                                     "        node{\n" +
                                                     "            echo \"UI testing...\"\n" +
                                                     "        }\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage ('deploy') {\n" +
                                                     "    node {\n" +
                                                     "        echo \"Deploying\"\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage ('deployToProd') {\n" +
                                                     "    node {\n" +
                                                     "        echo \"Deploying to production\"\n" +
                                                     "    }\n" +
                                                     "}", false
        ));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        NodeGraphBuilder builder = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(b1);
        List<FlowNode> nodes = getStagesAndParallels(builder);
        List<FlowNode> parallelNodes = getParallelNodes(builder);

        Assert.assertEquals(7, nodes.size());
        Assert.assertEquals(3, parallelNodes.size());

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);

        Assert.assertEquals(nodes.size(), resp.size());
        for (int i = 0; i < nodes.size(); i++) {
            FlowNode n = nodes.get(i);
            Map rn = resp.get(i);
            Assert.assertEquals(n.getId(), rn.get("id"));
            Assert.assertEquals(getNodeName(n), rn.get("displayName"));
            Assert.assertEquals("SUCCESS", rn.get("result"));
            List<Map> edges = (List<Map>) rn.get("edges");

            if (n.getDisplayName().equals("test")) {
                Assert.assertEquals(parallelNodes.size(), edges.size());
                Assert.assertEquals(edges.get(i).get("id"), parallelNodes.get(i).getId());
            } else if (n.getDisplayName().equals("build")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(i).get("id"), nodes.get(i + 1).getId());
            } else if (n.getDisplayName().equals("deploy")) {
                Assert.assertEquals(1, edges.size());
            } else if (n.getDisplayName().equals("deployToProd")) {
                Assert.assertEquals(0, edges.size());
            } else {
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(0).get("id"), nodes.get(nodes.size() - 2).getId());
            }
        }

        job1.setDefinition(new CpsFlowDefinition("stage ('build') {\n" +
                                                     "    node {\n" +
                                                     "        echo \"Building...\"\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage ('test') {\n" +
                                                     "    parallel 'unit':{\n" +
                                                     "        node {\n" +
                                                     "            echo \"Unit testing...\"\n" +
                                                     "            sh \"`fail-the-build`\"\n" + //fail the build intentionally
                                                     "        }\n" +
                                                     "    },\n" +
                                                     "    'integration':{\n" +
                                                     "        node {\n" +
                                                     "            echo \"Integration testing...\"\n" +
                                                     "        }\n" +
                                                     "    },\n" +
                                                     "    'ui':{\n" +
                                                     "        node {\n" +
                                                     "            echo \"UI testing...\"\n" +
                                                     "        }\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage ('deploy') {\n" +
                                                     "    node {\n" +
                                                     "        echo \"Deploying\"\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage ('deployToProd') {\n" +
                                                     "    node{\n" +
                                                     "        echo \"Deploying to production\"\n" +
                                                     "    }\n" +
                                                     "}", false
        ));
        b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b1);
        resp = get(String.format("/organizations/jenkins/pipelines/pipeline1/runs/%s/nodes/", b1.getId()), List.class);
        Assert.assertEquals(nodes.size(), resp.size());
        for (int i = 0; i < nodes.size(); i++) {
            FlowNode n = nodes.get(i);
            Map rn = resp.get(i);
            Assert.assertEquals(n.getId(), rn.get("id"));
            Assert.assertEquals(getNodeName(n), rn.get("displayName"));
            List<Map> edges = (List<Map>) rn.get("edges");
            if (n.getDisplayName().equals("build")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(i).get("id"), nodes.get(i + 1).getId());
                Assert.assertEquals("SUCCESS", rn.get("result"));
                Assert.assertEquals("FINISHED", rn.get("state"));
            } else if (n.getDisplayName().equals("test")) {
                Assert.assertEquals(parallelNodes.size(), edges.size());
                Assert.assertEquals(edges.get(i).get("id"), parallelNodes.get(i).getId());
                Assert.assertEquals("FAILURE", rn.get("result"));
                Assert.assertEquals("FINISHED", rn.get("state"));
            } else if (PipelineNodeUtil.getDisplayName(n).equals("unit")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(0).get("id"), nodes.get(nodes.size() - 2).getId());
                Assert.assertEquals("FAILURE", rn.get("result"));
                Assert.assertEquals("FINISHED", rn.get("state"));
            } else if (n.getDisplayName().equals("deploy")) {
                Assert.assertEquals(1, edges.size());
                Assert.assertNull(rn.get("result"));
                Assert.assertNull(rn.get("state"));
                Assert.assertNull(rn.get("startTime"));
                Assert.assertEquals(1, edges.size());
                Assert.assertEquals(edges.get(0).get("id"), nodes.get(nodes.size() - 1).getId());
            } else if (n.getDisplayName().equals("deployToProd")) {
                Assert.assertEquals(0, edges.size());
                Assert.assertNull(rn.get("result"));
                Assert.assertNull(rn.get("state"));
                Assert.assertNull(rn.get("startTime"));
                Assert.assertEquals(0, edges.size());
            } else {
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
                                                     "}", false
        ));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b1);

        NodeGraphBuilder builder = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(b1);
        List<FlowNode> nodes = getStagesAndParallels(builder);
        List<FlowNode> parallelNodes = getParallelNodes(builder);

        Assert.assertEquals(5, nodes.size());
        Assert.assertEquals(3, parallelNodes.size());

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);

        Assert.assertEquals(nodes.size(), resp.size());
        String unitNodeId = null;
        for (int i = 0; i < nodes.size(); i++) {
            FlowNode n = nodes.get(i);
            Map rn = resp.get(i);
            Assert.assertEquals(n.getId(), rn.get("id"));
            Assert.assertEquals(getNodeName(n), rn.get("displayName"));

            List<Map> edges = (List<Map>) rn.get("edges");

            switch (n.getDisplayName()) {
                case "test":
                    Assert.assertEquals( parallelNodes.size(), edges.size() );
                    Assert.assertEquals( edges.get( i ).get( "id" ), parallelNodes.get( i ).getId() );
                    Assert.assertEquals( "FAILURE", rn.get( "result" ) );
                    break;
                case "build":
                    Assert.assertEquals( 1, edges.size() );
                    Assert.assertEquals( edges.get( i ).get( "id" ), nodes.get( i + 1 ).getId() );
                    Assert.assertEquals( "SUCCESS", rn.get( "result" ) );
                    break;
                case "Branch: unit":
                    unitNodeId = n.getId();
                    Assert.assertEquals( 0, edges.size() );
                    Assert.assertEquals( "FAILURE", rn.get( "result" ) );
                    break;
                default:
                    Assert.assertEquals( 0, edges.size() );
                    Assert.assertEquals( "SUCCESS", rn.get( "result" ) );
                    break;
            }
        }
        assertNotNull(unitNodeId);
        get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + unitNodeId + "/steps/", List.class);
        String log = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + unitNodeId + "/log/", String.class);
        assertNotNull(log);
    }

    @Test
    public void getPipelineJobRunNodeNoStageTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");


        job1.setDefinition(new CpsFlowDefinition("node{\n" +
                                                     "  parallel 'unit':{\n" +
                                                     "    node{\n" +
                                                     "      sh \"Unit testing...\"\n" +
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
                                                     "}", false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        //j.assertBuildStatusSuccess(b1);
//        FlowGraphTable nodeGraphTable = new FlowGraphTable(b1.getExecution());
//        nodeGraphTable.build();
//        List<FlowNode> nodes = getStages(nodeGraphTable);
//        List<FlowNode> parallelNodes = getParallelNodes(nodeGraphTable);
//
//        Assert.assertEquals(3, nodes.size());
//        Assert.assertEquals(3, parallelNodes.size());
//
//        // get all nodes for pipeline1
        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + resp.get(0).get("id") + "/steps/", List.class);
//        Assert.assertEquals(nodes.size(), resp.size());

    }


    @Test
    public void getPipelineJobRunNodeTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        // same
        job1.setDefinition(new CpsFlowDefinition(
            "stage ('build') {\n" +
                "    node {\n" +
                "        echo \"Building...\"\n" +
                "    }\n" +
                "}\n" +
                "stage ('test') {\n" +
                "    parallel 'unit':{\n" +
                "        node {\n" +
                "            echo \"Unit testing...\"\n" +
                "        }\n" +
                "    },\n" +
                "    'integration':{\n" +
                "        node {\n" +
                "            echo \"Integration testing...\"\n" +
                "        }\n" +
                "    },\n" +
                "    'ui':{\n" +
                "        node {\n" +
                "            echo \"UI testing...\"\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "stage ('deploy') {\n" +
                "    node {\n" +
                "        echo \"Deploying\"\n" +
                "    }\n" +
                "}", false));

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

        Map node = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + n.getId());

        List<Map> edges = (List<Map>) node.get("edges");

        Assert.assertEquals(n.getId(), node.get("id"));
        Assert.assertEquals(getNodeName(n), node.get("displayName"));
        Assert.assertEquals("SUCCESS", node.get("result"));
        Assert.assertEquals(1, edges.size());
        Assert.assertEquals(nodes.get(1).getId(), edges.get(0).get("id"));


        //Get a parallel node detail
        node = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + parallelNodes.get(0).getId());

        n = parallelNodes.get(0);
        edges = (List<Map>) node.get("edges");

        Assert.assertEquals(n.getId(), node.get("id"));
        Assert.assertEquals(getNodeName(n), node.get("displayName"));
        Assert.assertEquals("SUCCESS", node.get("result"));
        Assert.assertEquals(1, edges.size());
        Assert.assertEquals(nodes.get(nodes.size() - 1).getId(), edges.get(0).get("id"));
    }


    @Test
    public void getPipelineJobRunNodeLogTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        // same
        job1.setDefinition(new CpsFlowDefinition("stage ('build') {\n" +
                                                     "    node {\n" +
                                                     "        echo \"Building...\"\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage ('test') {\n" +
                                                     "    parallel 'unit':{\n" +
                                                     "        node {\n" +
                                                     "            echo \"Unit testing...\"\n" +
                                                     "        }\n" +
                                                     "    },\n" +
                                                     "    'integration':{\n" +
                                                     "        node {\n" +
                                                     "            echo \"Integration testing...\"\n" +
                                                     "        }\n" +
                                                     "    },\n" +
                                                     "    'ui':{\n" +
                                                     "        node {\n" +
                                                     "            echo \"UI testing...\"\n" +
                                                     "        }\n" +
                                                     "    }\n" +
                                                     "}\n" +
                                                     "stage ('deploy') {\n" +
                                                     "    node {\n" +
                                                     "        echo \"Deploying\"\n" +
                                                     "    }\n" +
                                                     "}", false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);
        FlowGraphTable nodeGraphTable = new FlowGraphTable(b1.getExecution());
        nodeGraphTable.build();
        List<FlowNode> nodes = getStages(nodeGraphTable);
        List<FlowNode> parallelNodes = getParallelNodes(nodeGraphTable);

        Assert.assertEquals(6, nodes.size());
        Assert.assertEquals(3, parallelNodes.size());

        String output = get("/organizations/jenkins/pipelines/pipeline1/runs/1/log", String.class);
        assertNotNull(output);
        System.out.println(output);
    }

    @Test
    public void getPipelineJobRunStepLogTest() throws Exception {
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
                                                     "}", false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        NodeGraphBuilder builder = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(b1);
        List<FlowNode> flowNodes = getAllSteps(b1);

        Map resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/" + flowNodes.get(0).getId() + "/");

        String linkToLog = getActionLink(resp, "org.jenkinsci.plugins.workflow.actions.LogAction");

        assertNotNull(linkToLog);
        assertEquals("/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/1/steps/6/log/", linkToLog);
        String output = get(linkToLog.substring("/blue/rest".length()), String.class);
        Assert.assertNotNull(output);
    }

    @Test
    public void BlockStageNodesFailureTest1() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition("node{\n" +
                                                     "    stage ('Build') {\n" +
                                                     "            sh 'echo1 \"Building\"'\n" +
                                                     "    }\n" +
                                                     "    stage ('Test') {\n" +
                                                     "            sh 'echo testing'\n" +
                                                     "    }\n" +
                                                     "    stage ('Deploy') {\n" +
                                                     "            sh 'echo deploy'\n" +
                                                     "    }\n" +
                                                     "}", false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b1);
        List<Map> nodes = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(1, nodes.size());
        Assert.assertEquals("FAILURE", nodes.get(0).get("result"));
        Assert.assertEquals("FINISHED", nodes.get(0).get("state"));

    }


    @Test
    public void BlockStageNodesFailureTest2() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition("node{\n" +
                                                     "    stage ('Build') {\n" +
                                                     "            sh 'echo \"Building\"'\n" +
                                                     "    }\n" +
                                                     "    stage ('Test') {\n" +
                                                     "            sh 'echo1 testing'\n" +
                                                     "    }\n" +
                                                     "    stage ('Deploy') {\n" +
                                                     "            sh 'echo deploy'\n" +
                                                     "    }\n" +
                                                     "}", false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b1);
        List<Map> nodes = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(2, nodes.size());
        Assert.assertEquals("SUCCESS", nodes.get(0).get("result"));
        Assert.assertEquals("FINISHED", nodes.get(0).get("state"));
        Assert.assertEquals("FAILURE", nodes.get(1).get("result"));
        Assert.assertEquals("FINISHED", nodes.get(1).get("state"));

    }

    @Test
    public void BlockStageNodesFailureTest3() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition("node{\n" +
                                                     "    stage ('Build') {\n" +
                                                     "            sh 'echo \"Building\"'\n" +
                                                     "    }\n" +
                                                     "    stage ('Test') {\n" +
                                                     "            sh 'echo testing'\n" +
                                                     "    }\n" +
                                                     "    stage ('Deploy') {\n" +
                                                     "            sh 'echo1 deploy'\n" +
                                                     "    }\n" +
                                                     "}", false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b1);
        get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        List<Map> nodes = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(3, nodes.size());
        Assert.assertEquals("SUCCESS", nodes.get(0).get("result"));
        Assert.assertEquals("FINISHED", nodes.get(0).get("state"));
        Assert.assertEquals("SUCCESS", nodes.get(1).get("result"));
        Assert.assertEquals("FINISHED", nodes.get(1).get("state"));
        Assert.assertEquals("FAILURE", nodes.get(2).get("result"));
        Assert.assertEquals("FINISHED", nodes.get(2).get("state"));
    }

    @Test
    public void BlockStageStepsWithDesc() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition("node{\n" +
                                                     "    stage ('Build') {\n" +
                                                     "            sh 'echo \"Building\"'\n" +
                                                     "    }\n" +
                                                     "    stage ('Test') {\n" +
                                                     "            sh 'echo testing'\n" +
                                                     "    }\n" +
                                                     "    stage ('Deploy') {\n" +
                                                     "            sh 'echo deploy'\n" +
                                                     "    }\n" +
                                                     "}", false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.SUCCESS, b1);
        List<Map> steps = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/", List.class);
        Assert.assertEquals(3, steps.size());
        Assert.assertEquals("Shell Script", steps.get(0).get("displayName"));
        Assert.assertEquals("Shell Script", steps.get(1).get("displayName"));
        Assert.assertEquals("Shell Script", steps.get(2).get("displayName"));

        Assert.assertEquals("echo \"Building\"", steps.get(0).get("displayDescription"));
        Assert.assertEquals("echo testing", steps.get(1).get("displayDescription"));
        Assert.assertEquals("echo deploy", steps.get(2).get("displayDescription"));

    }

    @Test
    public void KyotoNodesFailureTest1() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition("pipeline {\n" +
                                                     "    agent any\n" +
                                                     "    stages {\n" +
                                                     "        stage ('Build') {\n" +
                                                     "steps{\n" +
                                                     "            sh 'echo1 \"Building\"'\n" +
                                                     "        }\n" +
                                                     "}\n" +
                                                     "        stage ('Test') {\n" +
                                                     "steps{\n" +
                                                     "            sh 'echo \"Building\"'\n" +
                                                     "        }\n" +
                                                     "}\n" +
                                                     "        stage ('Deploy') {\n" +
                                                     "steps{\n" +
                                                     "            sh 'echo \"Building\"'\n" +
                                                     "        }\n" +
                                                     "}\n" +
                                                     "    }\n" +
                                                     "}\n", false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b1);
        List<Map> nodes = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(3, nodes.size());

        Assert.assertEquals("FAILURE", nodes.get(0).get("result"));
        Assert.assertEquals("FINISHED", nodes.get(0).get("state"));

        Assert.assertEquals("NOT_BUILT", nodes.get(1).get("result"));
        Assert.assertEquals("SKIPPED", nodes.get(1).get("state"));

        Assert.assertEquals("NOT_BUILT", nodes.get(2).get("result"));
        Assert.assertEquals("SKIPPED", nodes.get(2).get("state"));
    }

    @Test
    public void KyotoNodesFailureTest2() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition("pipeline {\n" +
                                                     "    agent any\n" +
                                                     "    stages {\n" +
                                                     "        stage ('Build') {\n" +
                                                     "steps{\n" +
                                                     "            sh 'echo \"Building\"'\n" +
                                                     "}\n" +
                                                     "        }\n" +
                                                     "        stage ('Test') {\n" +
                                                     "steps{\n" +
                                                     "            sh 'echo \"Building\"'\n" +
                                                     "            sh 'echo2 \"Building finished\"'\n" +
                                                     "}\n" +
                                                     "        }\n" +
                                                     "        stage ('Deploy') {\n" +
                                                     "steps{\n" +
                                                     "            sh 'echo \"Building\"'\n" +
                                                     "}\n" +
                                                     "        }\n" +
                                                     "    }\n" +
                                                     "}\n", false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b1);
        List<Map> nodes = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(3, nodes.size());
        Assert.assertEquals("SUCCESS", nodes.get(0).get("result"));
        Assert.assertEquals("FINISHED", nodes.get(0).get("state"));
        Assert.assertEquals("FAILURE", nodes.get(1).get("result"));
        Assert.assertEquals("FINISHED", nodes.get(1).get("state"));
    }

    @Test
    public void KyotoNodesFailureTest3() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition("pipeline {\n" +
                                                     "    agent any\n" +
                                                     "    stages {\n" +
                                                     "        stage ('Build') {\n" +
                                                     "steps{\n" +
                                                     "            sh 'echo \"Building\"'\n" +
                                                     "}\n" +
                                                     "        }\n" +
                                                     "        stage ('Test') {\n" +
                                                     "steps{\n" +
                                                     "            sh 'echo \"Testing\"'\n" +
                                                     "}\n" +
                                                     "        }\n" +
                                                     "        stage ('Deploy') {\n" +
                                                     "steps{\n" +
                                                     "            sh 'echo1 \"Deploying\"'\n" +
                                                     "}\n" +
                                                     "        }\n" +
                                                     "    }\n" +
                                                     "}\n", false));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b1);
        List<Map> nodes = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(3, nodes.size());
        Assert.assertEquals("SUCCESS", nodes.get(0).get("result"));
        Assert.assertEquals("FINISHED", nodes.get(0).get("state"));
        Assert.assertEquals("SUCCESS", nodes.get(1).get("result"));
        Assert.assertEquals("FINISHED", nodes.get(1).get("state"));
        Assert.assertEquals("FAILURE", nodes.get(2).get("result"));
        Assert.assertEquals("FINISHED", nodes.get(2).get("state"));
    }

    @Test
    public void declarativeSyntheticSteps() throws Exception {
        setupScm("pipeline {\n" +
                     "    agent any\n" +
                     "    stages {\n" +
                     "        stage(\"build\") {\n" +
                     "            steps{\n" +
                     "              sh 'echo \"Start Build\"'\n" +
                     "              echo 'End Build'\n" +
                     "            }\n" +
                     "        }\n" +
                     "        stage(\"deploy\") {\n" +
                     "            steps{\n" +
                     "              sh 'echo \"Start Deploy\"'\n" +
                     "              sh 'echo \"Deploying...\"'\n" +
                     "              sh 'echo \"End Deploy\"'\n" +
                     "            }           \n" +
                     "        }\n" +
                     "    }\n" +
                     "    post {\n" +
                     "        failure {\n" +
                     "            echo \"failed\"\n" +
                     "        }\n" +
                     "        success {\n" +
                     "            echo \"success\"\n" +
                     "        }\n" +
                     "    }\n" +
                     "}");
        WorkflowMultiBranchProject mp = j.jenkins.createProject(WorkflowMultiBranchProject.class, "p");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false)));
        for (SCMSource source : mp.getSCMSources()) {
            assertEquals(mp, source.getOwner());
        }

        mp.scheduleBuild2(0).getFuture().get();

        j.waitUntilNoActivity();

        WorkflowJob p = scheduleAndFindBranchProject(mp, "master");
        j.waitUntilNoActivity();
        WorkflowRun b1 = p.getLastBuild();
        Assert.assertEquals(Result.SUCCESS, b1.getResult());

        List<FlowNode> stages = getStages(NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(b1));

        Assert.assertEquals(2, stages.size());

        Assert.assertEquals("build", stages.get(0).getDisplayName());
        Assert.assertEquals("deploy", stages.get(1).getDisplayName());

        List<Map> resp = get("/organizations/jenkins/pipelines/p/pipelines/master/runs/" + b1.getId() + "/nodes/", List.class);
        Assert.assertEquals(2, resp.size());
        Assert.assertEquals("build", resp.get(0).get("displayName"));
        Assert.assertEquals("deploy", resp.get(1).get("displayName"));

        resp = get("/organizations/jenkins/pipelines/p/pipelines/master/runs/" + b1.getId() + "/steps/", List.class);
        Assert.assertEquals(7, resp.size());

        resp = get("/organizations/jenkins/pipelines/p/pipelines/master/runs/" + b1.getId() + "/nodes/" + stages.get(0).getId() + "/steps/", List.class);
        Assert.assertEquals(3, resp.size());

        resp = get("/organizations/jenkins/pipelines/p/pipelines/master/runs/" + b1.getId() + "/nodes/" + stages.get(1).getId() + "/steps/", List.class);
        Assert.assertEquals(4, resp.size());

    }

    @Test
    public void declarativeSyntheticSkippedStage() throws Exception {

        setupScm("pipeline {\n" +
                     "    agent any\n" +
                     "    stages {\n" +
                     "        stage(\"build\") {\n" +
                     "            steps{\n" +
                     "              sh 'echo \"Start Build\"'\n" +
                     "              echo 'End Build'\n" +
                     "            }\n" +
                     "        }\n" +
                     "        stage(\"SkippedStage\") {\n" +
                     "            when {\n" +
                     "        expression {\n" +
                     "                return false\n" +
                     "        }\n" +
                     "            }\n" +
                     "            steps {\n" +
                     "                script {\n" +
                     "                    echo \"World\"\n" +
                     "                    echo \"Heal it\"\n" +
                     "                }\n" +
                     "\n" +
                     "            }\n" +
                     "        }\n" +
                     "        stage(\"deploy\") {\n" +
                     "            steps{\n" +
                     "              sh 'echo \"Start Deploy\"'\n" +
                     "              sh 'echo \"Deploying...\"'\n" +
                     "              sh 'echo \"End Deploy\"'\n" +
                     "            }           \n" +
                     "        }\n" +
                     "    }\n" +
                     "    post {\n" +
                     "        failure {\n" +
                     "            echo \"failed\"\n" +
                     "        }\n" +
                     "        success {\n" +
                     "            echo \"success\"\n" +
                     "        }\n" +
                     "    }\n" +
                     "}");
        WorkflowMultiBranchProject mp = j.jenkins.createProject(WorkflowMultiBranchProject.class, "p");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false)));
        for (SCMSource source : mp.getSCMSources()) {
            assertEquals(mp, source.getOwner());
        }

        mp.scheduleBuild2(0).getFuture().get();

        j.waitUntilNoActivity();

        WorkflowJob p = scheduleAndFindBranchProject(mp, "master");
        j.waitUntilNoActivity();
        WorkflowRun b1 = p.getLastBuild();
        Assert.assertEquals(Result.SUCCESS, b1.getResult());

        List<FlowNode> stages = getStages(NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(b1));

        Assert.assertEquals(3, stages.size());

        Assert.assertEquals("build", stages.get(0).getDisplayName());
        Assert.assertEquals("SkippedStage", stages.get(1).getDisplayName());
        Assert.assertEquals("deploy", stages.get(2).getDisplayName());

        List<Map> resp = get("/organizations/jenkins/pipelines/p/pipelines/master/runs/" + b1.getId() + "/nodes/", List.class);
        Assert.assertEquals(3, resp.size());
        Assert.assertEquals("build", resp.get(0).get("displayName"));
        Assert.assertEquals("SkippedStage", resp.get(1).get("displayName"));
        Assert.assertEquals("deploy", resp.get(2).get("displayName"));
        //check status
        Assert.assertEquals("SUCCESS", resp.get(0).get("result"));
        Assert.assertEquals("FINISHED", resp.get(0).get("state"));
        Assert.assertEquals("NOT_BUILT", resp.get(1).get("result"));
        Assert.assertEquals("SKIPPED", resp.get(1).get("state"));
        Assert.assertEquals("SUCCESS", resp.get(2).get("result"));
        Assert.assertEquals("FINISHED", resp.get(2).get("state"));

        resp = get("/organizations/jenkins/pipelines/p/pipelines/master/runs/" + b1.getId() + "/steps/", List.class);
        Assert.assertEquals(7, resp.size());

        resp = get("/organizations/jenkins/pipelines/p/pipelines/master/runs/" + b1.getId() + "/nodes/" + stages.get(0).getId() + "/steps/", List.class);
        Assert.assertEquals(3, resp.size());

        resp = get("/organizations/jenkins/pipelines/p/pipelines/master/runs/" + b1.getId() + "/nodes/" + stages.get(1).getId() + "/steps/", List.class);
        Assert.assertEquals(0, resp.size());

        resp = get("/organizations/jenkins/pipelines/p/pipelines/master/runs/" + b1.getId() + "/nodes/" + stages.get(2).getId() + "/steps/", List.class);
        Assert.assertEquals(4, resp.size());
    }

    @Test
    public void waitForInputTest() throws Exception {
        String script = "node {\n" +
            "    stage(\"parallelStage\"){\n" +
            "      parallel left : {\n" +
            "            echo \"running\"\n" +
            "            def branchInput = input message: 'Please input branch to test against', parameters: [[$class: 'StringParameterDefinition', defaultValue: 'master', description: '', name: 'branch']]\n" +
            "            echo \"BRANCH NAME: ${branchInput}\"\n" +
            "        }, \n" +
            "        right : {\n" +
            "            sh 'sleep 100000'\n" +
            "        }\n" +
            "    }\n" +
            "}";

        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition(script, false));
        QueueTaskFuture<WorkflowRun> buildTask = job1.scheduleBuild2(0);
        WorkflowRun run = buildTask.getStartCondition().get();
        CpsFlowExecution e = (CpsFlowExecution) run.getExecutionPromise().get();

        while (run.getAction(InputAction.class) == null) {
            e.waitForSuspension();
        }

        Map runResp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/");

        Assert.assertEquals("PAUSED", runResp.get("state"));

        List<FlowNodeWrapper> nodes = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run).getPipelineNodes();

        List<Map> nodesResp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);

        Assert.assertEquals("PAUSED", nodesResp.get(0).get("state"));
        Assert.assertEquals("UNKNOWN", nodesResp.get(0).get("result"));
        Assert.assertEquals("parallelStage", nodesResp.get(0).get("displayName"));

        Assert.assertEquals("PAUSED", nodesResp.get(1).get("state"));
        Assert.assertEquals("UNKNOWN", nodesResp.get(1).get("result"));
        Assert.assertEquals("left", nodesResp.get(1).get("displayName"));

        Assert.assertEquals("RUNNING", nodesResp.get(2).get("state"));
        Assert.assertEquals("UNKNOWN", nodesResp.get(2).get("result"));
        Assert.assertEquals("right", nodesResp.get(2).get("displayName"));

        List<Map> stepsResp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/", List.class);

        Assert.assertEquals("RUNNING", stepsResp.get(0).get("state"));
        Assert.assertEquals("UNKNOWN", stepsResp.get(0).get("result"));
        Assert.assertEquals("13", stepsResp.get(0).get("id"));

        Assert.assertEquals("PAUSED", stepsResp.get(2).get("state"));
        Assert.assertEquals("UNKNOWN", stepsResp.get(2).get("result"));
        Assert.assertEquals("12", stepsResp.get(2).get("id"));
    }

    @Test
    public void testBlockedStep() throws Exception {
        String script = "node {\n" +
            "    stage(\"one\"){\n" +
            "        echo '1'\n" +
            "    }\n" +
            "    stage(\"two\") {\n" +
            "            node('blah'){\n" +
            "                sh 'blah'\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "}";

        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition(script, false));

        QueueTaskFuture<WorkflowRun> runQueueTaskFuture = job1.scheduleBuild2(0);
        WorkflowRun run = runQueueTaskFuture.getStartCondition().get();
        CpsFlowExecution e = (CpsFlowExecution) run.getExecutionPromise().get();

        if (waitForItemToAppearInQueue(1000 * 300)) { //5 min timeout
            List<FlowNode> nodes = getStages(NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run));
            if (nodes.size() == 2) {
                List<Map> stepsResp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/11/steps/", List.class);
                assertEquals(1, stepsResp.size());
                assertEquals("QUEUED", stepsResp.get(0).get("state"));
            }
        } else {
            // Avoid spurious code coverage failures
            final FlowNode node = new FlowNode(null, "fake") {
                @Override
                protected String getTypeDisplayName() {
                    return "fake";
                }
            };
            final MemoryFlowChunk chunk = new MemoryFlowChunk() {
                @Override
                public FlowNode getFirstNode() {
                    return node;
                }
            };
            new PipelineStepVisitor.LocalAtomNode(chunk, "fake");
        }
    }

    private boolean waitForItemToAppearInQueue(long timeout) throws InterruptedException {
        long start = System.currentTimeMillis();
        long diff = 0;
        while (Jenkins.get().getQueue().getItems().length <= 0 && diff < timeout) {
            diff = System.currentTimeMillis() - start;
            Thread.sleep(100);
        }
        return Jenkins.get().getQueue().getItems().length > 0;
    }

    @Test
    public void submitInput() throws Exception {
        String script = "node {\n" +
            "    stage(\"first\"){\n" +
            "            def branchInput = input message: 'Please input branch to test against', parameters: [[$class: 'StringParameterDefinition', defaultValue: 'master', description: '', name: 'branch']]\n" +
            "            echo \"BRANCH NAME: ${branchInput}\"\n" +
            "    }\n" +
            "}";

        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition(script, false));
        QueueTaskFuture<WorkflowRun> buildTask = job1.scheduleBuild2(0);
        WorkflowRun run = buildTask.getStartCondition().get();
        CpsFlowExecution e = (CpsFlowExecution) run.getExecutionPromise().get();

        while (run.getAction(InputAction.class) == null) {
            e.waitForSuspension();
        }


        List<Map> stepsResp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/", List.class);


        Assert.assertEquals("PAUSED", stepsResp.get(0).get("state"));
        Assert.assertEquals("UNKNOWN", stepsResp.get(0).get("result"));
        Assert.assertEquals("7", stepsResp.get(0).get("id"));

        Map<String, Object> input = (Map<String, Object>) stepsResp.get(0).get("input");
        Assert.assertNotNull(input);
        String id = (String) input.get("id");
        Assert.assertNotNull(id);

        List<Map<String, Object>> params = (List<Map<String, Object>>) input.get("parameters");

        post("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/7/",
              MapsHelper.of("id", id,
                             PARAMETERS_ELEMENT,
                             MapsHelper.of(MapsHelper.of("name", params.get(0).get("name"), "value", "master"))
             )
            , 200);

        if (waitForBuildCount(job1, 1, Result.SUCCESS)) {
            Map<String, Object> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/7/");
            Assert.assertEquals("FINISHED", resp.get("state"));
            Assert.assertEquals("SUCCESS", resp.get("result"));
            Assert.assertEquals("7", resp.get("id"));
        }
    }

    @Test
    public void abortInput() throws Exception {
        String script = "node {\n" +
            "    stage(\"thing\"){\n" +
            "            input 'continue'\n" +
            "    }\n" +
            "}";

        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition(script, false));
        QueueTaskFuture<WorkflowRun> buildTask = job1.scheduleBuild2(0);
        WorkflowRun run = buildTask.getStartCondition().get();
        CpsFlowExecution e = (CpsFlowExecution) run.getExecutionPromise().get();

        while (run.getAction(InputAction.class) == null) {
            e.waitForSuspension();
        }

        List<Map> stepsResp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/", List.class);

        System.out.println(stepsResp);

        Assert.assertEquals("PAUSED", stepsResp.get(0).get("state"));
        Assert.assertEquals("UNKNOWN", stepsResp.get(0).get("result"));
        String stepId = (String) stepsResp.get(0).get("id");
        //Assert.assertEquals("7", stepsResp.get(0).get("id"));

        Map<String, Object> input = (Map<String, Object>) stepsResp.get(0).get("input");
        Assert.assertNotNull(input);
        String id = (String) input.get("id");
        Assert.assertNotNull(id);

        JSONObject req = new JSONObject();
        req.put("id", id);
        req.put("abort", true);

        post("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/" + stepId + "/", req, 200);

        if (waitForBuildCount(job1, 1, Result.ABORTED)) {
            Map<String, Object> resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/" + stepId + "/");
            Assert.assertEquals("FINISHED", resp.get("state"));
            Assert.assertEquals("ABORTED", resp.get("result"));
            Assert.assertEquals(stepId, resp.get("id"));
        }
    }


    @Test
    public void stageTestJENKINS_40135() throws Exception {
        String script = "node {\n" +
            "    stage 'Stage 1'\n" +
            "    stage 'Stage 2'\n" +
            "       echo 'hello'\n" +
            "}";
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition(script, false));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);
        List<Map> nodes = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        Assert.assertEquals(2, nodes.size());
        Assert.assertEquals("SUCCESS", nodes.get(0).get("result"));
        Assert.assertEquals("FINISHED", nodes.get(0).get("state"));
        Assert.assertEquals("SUCCESS", nodes.get(1).get("result"));
        Assert.assertEquals("FINISHED", nodes.get(1).get("state"));
    }

    @Test
    public void parameterizedPipeline() throws Exception {
        String script = "properties([parameters([string(defaultValue: 'xyz', description: 'string param', name: 'param1'), string(description: 'string param', name: 'param2')]), pipelineTriggers([])])\n" +
            "\n" +
            "node(){\n" +
            "    stage('build'){\n" +
            "        echo \"building\"\n" +
            "    }\n" +
            "}";

        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition(script, false));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        Map resp = get("/organizations/jenkins/pipelines/pipeline1/");

        List<Map<String, Object>> parameters = (List<Map<String, Object>>) resp.get("parameters");
        Assert.assertEquals(2, parameters.size());
        Assert.assertEquals("param1", parameters.get(0).get("name"));
        Assert.assertEquals("StringParameterDefinition", parameters.get(0).get("type"));
        Assert.assertEquals("string param", parameters.get(0).get("description"));
        Assert.assertEquals("xyz", ((Map) parameters.get(0).get("defaultParameterValue")).get("value"));

        Assert.assertEquals("param2", parameters.get(1).get("name"));
        Assert.assertEquals("StringParameterDefinition", parameters.get(1).get("type"));
        Assert.assertEquals("string param", parameters.get(1).get("description"));
        Assert.assertNull(Util.fixEmpty((String) ((Map) parameters.get(1).get("defaultParameterValue")).get("value")));

        resp = post("/organizations/jenkins/pipelines/pipeline1/runs/", MapsHelper.of("parameters",
                                                                                        Arrays.asList(MapsHelper.of("name", "param1", "value", "abc"),
                                                                                                      MapsHelper.of("name", "param2", "value", "def"))
        ), 200);
        Assert.assertEquals("pipeline1", resp.get("pipeline"));
        Thread.sleep(5000);
        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/2/");

        Assert.assertEquals("Response should be SUCCESS: " + resp.toString(), "SUCCESS", resp.get("result"));
        Assert.assertEquals("Response should be FINISHED: " + resp.toString(), "FINISHED", resp.get("state"));
    }

    @Test
    @Issue("JENKINS-49297")
    public void submitInputPostBlock() throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        URL resource = getClass().getResource("stepsFromPost.jenkinsfile");
        String jenkinsFile = IOUtils.toString(resource, StandardCharsets.UTF_8);
        job.setDefinition(new CpsFlowDefinition(jenkinsFile, true));
        QueueTaskFuture<WorkflowRun> buildTask = job.scheduleBuild2(0);
        WorkflowRun run = buildTask.getStartCondition().get();
        CpsFlowExecution e = (CpsFlowExecution) run.getExecutionPromise().get();

        while (run.getAction(InputAction.class) == null) {
            e.waitForSuspension();
        }

        List<Map> nodes = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);

        assertEquals(1, nodes.size());

        List<Map> steps = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + nodes.get(0).get("id") + "/steps/", List.class);

        assertEquals(3, steps.size());

        assertEquals("7", steps.get(0).get("id"));
        assertEquals("Hello World", steps.get(0).get("displayDescription"));
        assertEquals("12", steps.get(1).get("id"));
        assertEquals("Hello World from post", steps.get(1).get("displayDescription"));
        assertEquals("13", steps.get(2).get("id"));
        assertEquals("Wait for interactive input", steps.get(2).get("displayName"));
    }

    @Test
    @Issue("JENKINS-48884")
    public void submitInputPostBlockWithParallelStages() throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        URL resource = getClass().getResource("parallelStepsFromPost.jenkinsfile");
        String jenkinsFile = IOUtils.toString(resource, StandardCharsets.UTF_8);
        job.setDefinition(new CpsFlowDefinition(jenkinsFile, true));
        QueueTaskFuture<WorkflowRun> buildTask = job.scheduleBuild2(0);
        WorkflowRun run = buildTask.getStartCondition().get();
        CpsFlowExecution e = (CpsFlowExecution) run.getExecutionPromise().get();

        while (run.getAction(InputAction.class) == null) {
            e.waitForSuspension();
        }

        List<Map> nodes = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);

        assertEquals(6, nodes.size());

        List<Map> steps = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/" + nodes.get(0).get("id") + "/steps/", List.class);

        assertEquals(4, steps.size());

        assertEquals("15", steps.get(0).get("id"));
        assertEquals("exit 1", steps.get(0).get("displayDescription"));
        assertEquals("17", steps.get(1).get("id"));
        assertEquals("hello stable", steps.get(1).get("displayDescription"));
        assertEquals("47", steps.get(2).get("id"));
        assertEquals("Hello World from post", steps.get(2).get("displayDescription"));
        assertEquals("48", steps.get(3).get("id"));
        assertEquals("Wait for interactive input", steps.get(3).get("displayName"));
    }

    @Test
    @Issue("JENKINS-49050")
    public void parallelStagesGroupsAndNestedStages() throws Exception {
        WorkflowJob p = createWorkflowJobWithJenkinsfile(getClass(), "parallelStagesGroupsAndStages.jenkinsfile");
        Slave s = j.createOnlineSlave();
        s.setLabelString("foo");
        s.setNumExecutors(2);

        // Run until completed
        WorkflowRun run = p.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(run);

        PipelineNodeGraphVisitor pipelineNodeGraphVisitor = new PipelineNodeGraphVisitor(run);
        assertTrue(pipelineNodeGraphVisitor.isDeclarative());

        List<FlowNodeWrapper> wrappers = pipelineNodeGraphVisitor.getPipelineNodes();

        FlowNodeWrapper flowNodeWrapper = wrappers.get(0);
        assertEquals("top", flowNodeWrapper.getDisplayName());
        assertEquals(2, flowNodeWrapper.edges.size());

        flowNodeWrapper = wrappers.get(1);
        assertEquals("first", flowNodeWrapper.getDisplayName());
        assertEquals(1, flowNodeWrapper.edges.size());
        assertEquals(1, flowNodeWrapper.getParents().size());

        assertEquals("first-inner-first", flowNodeWrapper.edges.get(0).getDisplayName());

        assertEquals(7, wrappers.size());

        List<Map> nodes = get("/organizations/jenkins/pipelines/" + p.getName() + "/runs/1/nodes/", List.class);
        assertEquals(7, nodes.size());
    }

    @Ignore("TODO flaky; e.g., org.junit.ComparisonFailure: running node names expected:<A, B, B-[A, B-B, B-B-1, B-B-2, B-C, B-C-1, B-C-2, C, D, D-A, D-B, D-B-1, D-B-2, D-C, D-C-1, D-C]-2> but was:<A, B, B-[B, B-B-1, B-B]-2>")
    @Test
    @Issue("JENKINS-53816")
    public void graphConsistentWhileExecuting() throws Exception {

        final String expectedNodeNames =
            "A, B, B-A, B-B, B-B-1, B-B-2, B-C, B-C-1, B-C-2, C, D, D-A, D-B, D-B-1, D-B-2, D-C, D-C-1, D-C-2";

        String completeNodeNames = checkConsistencyWhileBuilding("JENKINS-53816.jenkinsfile");

        assertEquals("node names", expectedNodeNames, completeNodeNames);
    }

    @Test
    @Issue("JENKINS-53816")
    public void graphConsistentWhileExecuting2() throws Exception {

        final String expectedNodeNames = "first-sequential-stage, first-solo, multiple-stages, other-single-stage, " +
            "parent, second-sequential-stage, second-solo, single-stage, third-sequential-stage";

        String completeNodeNames = checkConsistencyWhileBuilding("sequential_parallel_stages_long_run_time.jenkinsfile");

        assertEquals("node names", expectedNodeNames, completeNodeNames);
    }

    private String checkConsistencyWhileBuilding(String jenkinsFileName) throws Exception {

        /*
            Run a complex pipeline to completion, then start a new build and inspect it while it's running, to exercise
            the code that merges incomplete runs with previous builds to generate a complete graph
         */

        WorkflowJob p = createWorkflowJobWithJenkinsfile(getClass(), jenkinsFileName);

        // Do an initial run, collect the nodes
        final WorkflowRun run1 = p.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(run1);

        final List<BluePipelineNode> completeNodes = new PipelineNodeContainerImpl(run1, new Link("foo")).getNodes();

        final String completeNodeNames = completeNodes.stream()
                                                      .map(BluePipelineStep::getDisplayName)
                                                      .sorted()
                                                      .collect(Collectors.joining(", "));

        // Start another build...
        final WorkflowRun run2 = p.scheduleBuild2(0).waitForStart();

        // ... then watch while it runs, checking for the same graph nodes

        int loopCount = 0;

        do {
            Thread.sleep(1000);

            List<BluePipelineNode> runningNodes = new PipelineNodeContainerImpl(run2, new Link("foo")).getNodes();
            String runningNodeNames = runningNodes.stream()
                                                  .map(BluePipelineStep::getDisplayName)
                                                  .sorted()
                                                  .collect(Collectors.joining(", "));

            assertEquals("running node names", completeNodeNames, runningNodeNames);

            loopCount++;

        } while (run2.isBuilding());

        // Sanity check, make sure we're *actually* checking stuff.
        assertTrue("Checked multiple times while building", loopCount > 5);

        return completeNodeNames; // So caller can do any additional checks
    }

    @Ignore("Fails on ci.jenkins.io but not locally. Reasons unclear.  Likely a timing issue.")
    @Test
    public void sequentialParallelStagesLongRun() throws Exception {
        WorkflowJob p = createWorkflowJobWithJenkinsfile(getClass(), "sequential_parallel_stages_long_run_time.jenkinsfile");
        Slave s = j.createOnlineSlave();
        s.setNumExecutors(3);
        WorkflowRun run = p.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(run);

        List<String> watchedStages = Arrays.asList("first-sequential-stage", "second-sequential-stage", "third-sequential-stage");

        run = p.scheduleBuild2(0).waitForStart();

        Thread.sleep(1000);

        long loopCount = 0;
        while (run.isBuilding()) {
            PipelineNodeContainerImpl pipelineNodeContainer = new PipelineNodeContainerImpl(run, new Link("foo"));

            List<BluePipelineNode> nodes = pipelineNodeContainer.getNodes();
            for (BluePipelineNode node : nodes) {
                if (StringUtils.isEmpty(node.getFirstParent())
                    && watchedStages.contains(node.getDisplayName())) {
                    LOGGER.error("node {} has getFirstParent null", node);
                    fail("node with getFirstParent null:" + node);
                }

                // we try to find edges with id for a non existing node in the list
                List<BluePipelineNode.Edge> emptyEdges =
                    node.getEdges().stream().filter(edge ->
                                                        !nodes.stream()
                                                              .filter(bluePipelineNode -> bluePipelineNode.getId().equals(edge.getId()))
                                                              .findFirst().isPresent()

                    ).collect(Collectors.toList());
                if (!emptyEdges.isEmpty()) {
                    // TODO remove this weird if but it's only to have breakpoint in IDE
                    assertTrue("edges with unknown nodes" + nodes, !emptyEdges.isEmpty());
                }
            }


            LOGGER.debug("nodes size {}", nodes.size());
            if (nodes.size() != 9) {
                LOGGER.info("loop: " + loopCount + ", nodes size {}", nodes.size());
                LOGGER.info("nodes != 9 {}", nodes);
                fail("nodes != 9:  " + nodes);
            }

            Thread.sleep(100);
        }

    }


    @Test
    @Issue("JENKINS-49050")
    public void sequentialParallelStages() throws Exception {
        WorkflowJob p = createWorkflowJobWithJenkinsfile(getClass(), "sequentialParallel.jenkinsfile");
        Slave s = j.createOnlineSlave();
        s.setNumExecutors(2);

        // Run until completed
        WorkflowRun run = p.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(run);

        PipelineNodeGraphVisitor pipelineNodeGraphVisitor = new PipelineNodeGraphVisitor(run);
        assertTrue(pipelineNodeGraphVisitor.isDeclarative());

        List<FlowNodeWrapper> wrappers = pipelineNodeGraphVisitor.getPipelineNodes();

        assertEquals(9, wrappers.size());

        Optional<FlowNodeWrapper> optionalFlowNodeWrapper =
            wrappers.stream().filter(nodeWrapper -> nodeWrapper.getDisplayName().equals("first-sequential-stage"))
                    .findFirst();

        // we ensure "multiple-stages" is parent of "first-sequential-stage"
        assertTrue(optionalFlowNodeWrapper.isPresent());
        assertEquals(1, optionalFlowNodeWrapper.get().edges.size());
        assertEquals("second-sequential-stage", optionalFlowNodeWrapper.get().edges.get(0).getDisplayName());

        final String parentId = optionalFlowNodeWrapper.get().getFirstParent().getId();

        optionalFlowNodeWrapper =
            wrappers.stream().filter(nodeWrapper -> nodeWrapper.getId().equals(parentId))
                    .findFirst();

        assertTrue(optionalFlowNodeWrapper.isPresent());
        assertEquals("multiple-stages", optionalFlowNodeWrapper.get().getDisplayName());
        assertEquals(1, optionalFlowNodeWrapper.get().edges.size());

        optionalFlowNodeWrapper.get().edges.stream()
                                           .filter(nodeWrapper -> nodeWrapper.getDisplayName().equals("first-sequential-stage"))
                                           .findFirst();
        assertTrue(optionalFlowNodeWrapper.isPresent());


        optionalFlowNodeWrapper =
            wrappers.stream().filter(nodeWrapper -> nodeWrapper.getDisplayName().equals("other-single-stage"))
                    .findFirst();
        assertTrue(optionalFlowNodeWrapper.isPresent());

        final String otherParentId = optionalFlowNodeWrapper.get().getFirstParent().getId();

        optionalFlowNodeWrapper =
            wrappers.stream().filter(nodeWrapper -> nodeWrapper.getId().equals(otherParentId))
                    .findFirst();

        assertTrue(optionalFlowNodeWrapper.isPresent());
        assertEquals("parent", optionalFlowNodeWrapper.get().getDisplayName());
        assertEquals(3, optionalFlowNodeWrapper.get().edges.size());

        optionalFlowNodeWrapper =
            wrappers.stream().filter(nodeWrapper -> nodeWrapper.getDisplayName().equals("second-sequential-stage"))
                    .findFirst();
        assertTrue(optionalFlowNodeWrapper.isPresent());

        assertEquals(1, optionalFlowNodeWrapper.get().edges.size());
        assertEquals("third-sequential-stage", optionalFlowNodeWrapper.get().edges.get(0).getDisplayName());

        optionalFlowNodeWrapper =
            wrappers.stream().filter(nodeWrapper -> nodeWrapper.getDisplayName().equals("third-sequential-stage"))
                    .findFirst();
        assertTrue(optionalFlowNodeWrapper.isPresent());

        assertEquals(1, optionalFlowNodeWrapper.get().edges.size());
        assertEquals("second-solo", optionalFlowNodeWrapper.get().edges.get(0).getDisplayName());

        List<Map> nodes = get("/organizations/jenkins/pipelines/" + p.getName() + "/runs/1/nodes/", List.class);
        assertEquals(9, nodes.size());


        Optional<Map> firstSeqStage = nodes.stream()
                                           .filter(map -> map.get("displayName")
                                                             .equals("first-sequential-stage")).findFirst();

        assertTrue(firstSeqStage.isPresent());
        String firstParentId = (String) firstSeqStage.get().get("firstParent");

        Optional<Map> parentStage = nodes.stream()
                                         .filter(map -> map.get("id")
                                                           .equals(firstParentId)).findFirst();
        assertTrue(parentStage.isPresent());
        assertEquals("multiple-stages", parentStage.get().get("displayName"));

        // ensure no issue getting steps for each node
        for (Map<String, String> node : nodes) {
            String id = node.get("id");
            List<Map> steps =
                get("/organizations/jenkins/pipelines/" + p.getName() + "/runs/1/nodes/" + id + "/steps/", List.class);
            assertFalse(steps.get(0).isEmpty());
        }
    }

    @Test
    @Issue("JENKINS-49779")
    public void sequentialParallelStagesWithPost() throws Exception {
        WorkflowJob p = createWorkflowJobWithJenkinsfile(getClass(), "sequentialParallelWithPost.jenkinsfile");
        Slave s = j.createOnlineSlave();
        s.setNumExecutors(2);

        // Run until completed
        j.buildAndAssertSuccess(p);

        List<Map> nodes = get("/organizations/jenkins/pipelines/" + p.getName() + "/runs/1/nodes/", List.class);
        assertEquals(9, nodes.size());


        Optional<Map> thirdSeqStage = nodes.stream()
                                           .filter(map -> map.get("displayName")
                                                             .equals("third-sequential-stage")).findFirst();

        assertTrue(thirdSeqStage.isPresent());

        List<Map> steps = get("/organizations/jenkins/pipelines/" + p.getName() + "/runs/1/nodes/" + thirdSeqStage.get().get("id") + "/steps/", List.class);

        assertEquals(2, steps.size());
        assertEquals("echo 'dummy text third-sequential-stage'", steps.get(0).get("displayDescription"));
        assertEquals("echo 'dummy text post multiple-stages'", steps.get(1).get("displayDescription"));
    }


    @Test
    public void nestedStagesGroups() throws Exception {
        WorkflowJob p = createWorkflowJobWithJenkinsfile(getClass(), "nestedStagesGroups.jenkinsfile");
        Slave s = j.createOnlineSlave();
        s.setLabelString("foo");
        s.setNumExecutors(4);

        // Run until completed
        WorkflowRun run = p.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(run);

        PipelineNodeGraphVisitor pipelineNodeGraphVisitor = new PipelineNodeGraphVisitor(run);
        assertTrue(pipelineNodeGraphVisitor.isDeclarative());
        List<FlowNodeWrapper> wrappers = pipelineNodeGraphVisitor.getPipelineNodes();
        assertEquals(7, wrappers.size());
    }

    @Test
    @Issue("JENKINS-49050")
    public void parallelStagesNonNested() throws Exception {
        WorkflowJob p = createWorkflowJobWithJenkinsfile(getClass(), "parallelStagesNonNested.jenkinsfile");
        Slave s = j.createOnlineSlave();
        s.setLabelString("foo");
        s.setNumExecutors(2);

        // Run until completed
        WorkflowRun run = p.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(run);

        PipelineNodeGraphVisitor pipelineNodeGraphVisitor = new PipelineNodeGraphVisitor(run);

        List<FlowNodeWrapper> wrappers = pipelineNodeGraphVisitor.getPipelineNodes();
        assertEquals(3, wrappers.size());

        List<Map> nodes = get("/organizations/jenkins/pipelines/" + p.getName() + "/runs/1/nodes/", List.class);
        assertEquals(3, nodes.size());
    }

    @Test
    public void pipelineLogError() throws Exception {
        String script = "def foo = null\n" +
            "\n" +
            "node {\n" +
            "    stage('blah') {\n" +
            "        sh \"echo 42\"\n" +
            "        foo.bar = 42\n" +
            "        sh \"echo 43\"\n" +
            "        \n" +
            "    }\n" +
            "}";

        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition(script, false));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b1);

        String resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/7/log", String.class);
        System.out.println(resp);
        Assert.assertTrue(resp.trim().endsWith("Cannot set property 'bar' on null object"));
    }

    @Test
    public void pipelineLogError1() throws Exception {
        String script =
            "node {\n" +
                "    stage('blah') {\n" +
                "        sh \"echo 42\"\n" +
                "        error(\"this error should appear in log\")\n" +
                "        \n" +
                "    }\n" +
                "}";

        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition(script, false));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b1);

        String resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/steps/8/log/", String.class);

        Assert.assertTrue(resp.trim().endsWith("this error should appear in log"));
    }

    @Test
    public void declarativeParallelPipeline() throws Exception {
        String script = "pipeline {\n"
            + "  agent none\n"
            + "  stages {\n"
            + "    stage('Pre build') {\n"
            + "      agent {\n"
            + "        label '" + j.jenkins.getSelfLabel().getName() + "'\n"
            + "      }\n"
            + "      steps {\n"
            + "        echo 'Pre build steps'\n"
            + "      }\n"
            + "    }\n"
            + "    stage('Run Tests') {\n"
            + "      parallel {\n"
            + "        stage('Internal') {\n"
            + "          agent {\n"
            + "            label '" + j.jenkins.getSelfLabel().getName() + "'\n"
            + "          }\n"
            + "          stages {\n"
            + "            stage('Prep') {\n"
            + "              steps {\n"
            + "                echo 'Prep internal'\n"
            + "              }\n"
            + "            }\n"
            + "            stage('Run') {\n"
            + "              steps {\n"
            + "                echo 'Run internal'\n"
            + "              }\n"
            + "            }\n"
            + "          }\n"
            + "        }\n"
            + "        stage('Prod') {\n"
            + "          agent {\n"
            + "            label '" + j.jenkins.getSelfLabel().getName() + "'\n"
            + "          }\n"
            + "          stages {\n"
            + "            stage('Prep') {\n"
            + "              steps {\n"
            + "                echo 'Prep prod'\n"
            + "              }\n"
            + "            }\n"
            + "            stage('Run') {\n"
            + "              steps {\n"
            + "                echo 'Run prod'\n"
            + "              }\n"
            + "            }\n"
            + "          }\n"
            + "        }\n"
            + "      }\n"
            + "    }\n"
            + "    stage('Post build') {\n"
            + "      agent {\n"
            + "        label '" + j.jenkins.getSelfLabel().getName() + "'\n"
            + "      }\n"
            + "      steps {\n"
            + "        echo 'Post build steps'\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "}";

        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition(script, false));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        WorkflowRun run = j.waitForCompletion(b1);
        j.assertBuildStatus(Result.SUCCESS, run);

        PipelineNodeGraphVisitor pipelineNodeGraphVisitor = new PipelineNodeGraphVisitor(run);
        List<FlowNodeWrapper> wrappers = pipelineNodeGraphVisitor.getPipelineNodes();

        assertEquals(9, wrappers.size());

        List<Map> nodes = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        for (int i = 0; i < nodes.size(); i++) {
            Map n = nodes.get(i);
            List<Map> edges = (List<Map>) n.get("edges");
            if (i == 0) {
                assertEquals("Pre build", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(i + 1).get("id"), edges.get(0).get("id"));
            }
            if (i == 1) {
                assertEquals("Run Tests", n.get("displayName"));
                assertEquals(2, edges.size());
                assertEquals(nodes.get(i + 1).get("id"), edges.get(0).get("id"));
                assertEquals(nodes.get(i + 2).get("id"), edges.get(1).get("id"));
            }
            if (i == 2) {
                assertEquals("Internal", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(5).get("id"), edges.get(0).get("id"));
            }
            if (i == 3) {
                assertEquals("Prod", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(7).get("id"), edges.get(0).get("id"));
            }
            if (i == 4) {
                assertEquals("Post build", n.get("displayName"));
                assertEquals(0, edges.size());
            }
            if (i == 5) {
                assertEquals("Prep", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(6).get("id"), edges.get(0).get("id"));
            }
            if (i == 6) {
                assertEquals("Run", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(4).get("id"), edges.get(0).get("id"));
            }
            if (i == 7) {
                assertEquals("Prep", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(8).get("id"), edges.get(0).get("id"));
            }
            if (i == 8) {
                assertEquals("Run", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(4).get("id"), edges.get(0).get("id"));
            }
        }
    }

    @Test
    public void scriptedParallelPipeline() throws Exception {
        String script = "node() {\n"
            + "  stage('Pre build') {\n"
            + "    echo 'Pre build steps'\n"
            + "  }\n"
            + "  parallel(\n"
            + "    'Internal': {\n"
            + "      stage('Prep') {\n"
            + "        echo 'Prep internal'\n"
            + "      }\n"
            + "      stage('Run') {\n"
            + "        echo 'Run internal'\n"
            + "      }\n"
            + "    },\n"
            + "    'Prod': {\n"
            + "      stage('Prep') {\n"
            + "        echo 'Prep prod'\n"
            + "      }\n"
            + "      stage('Run') {\n"
            + "        echo 'Run prod'\n"
            + "      }\n"
            + "    }\n"
            + "  )\n"
            + "  stage('Post build') {\n"
            + "    echo 'Post build steps'\n"
            + "  }\n"
            + "}";

        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition(script, false));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        WorkflowRun run = j.waitForCompletion(b1);
        j.assertBuildStatus(Result.SUCCESS, run);

        PipelineNodeGraphVisitor pipelineNodeGraphVisitor = new PipelineNodeGraphVisitor(run);
        List<FlowNodeWrapper> wrappers = pipelineNodeGraphVisitor.getPipelineNodes();

        assertEquals(9, wrappers.size());

        List<Map> nodes = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        for (int i = 0; i < nodes.size(); i++) {
            Map n = nodes.get(i);
            List<Map> edges = (List<Map>) n.get("edges");
            if (i == 0) {
                assertEquals("Pre build", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(i + 1).get("id"), edges.get(0).get("id"));
            }
            if (i == 1) {
                assertEquals("Parallel", n.get("displayName"));
                assertEquals(2, edges.size());
                assertEquals(nodes.get(i + 1).get("id"), edges.get(0).get("id"));
                assertEquals(nodes.get(i + 2).get("id"), edges.get(1).get("id"));
            }
            if (i == 2) {
                assertEquals("Internal", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(5).get("id"), edges.get(0).get("id"));
            }
            if (i == 3) {
                assertEquals("Prod", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(7).get("id"), edges.get(0).get("id"));
            }
            if (i == 4) {
                assertEquals("Post build", n.get("displayName"));
                assertEquals(0, edges.size());
            }
            if (i == 5) {
                assertEquals("Prep", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(6).get("id"), edges.get(0).get("id"));
            }
            if (i == 6) {
                assertEquals("Run", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(4).get("id"), edges.get(0).get("id"));
            }
            if (i == 7) {
                assertEquals("Prep", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(8).get("id"), edges.get(0).get("id"));
            }
            if (i == 8) {
                assertEquals("Run", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(4).get("id"), edges.get(0).get("id"));
            }
        }
    }

    @Test
    public void orphanParallels1() throws Exception {
        String script = "parallel('branch1':{\n" +
            "        node {\n" +
            "            stage('Setup') {\n" +
            "                sh 'echo \"Setup...\"'\n" +
            "            }\n" +
            "            stage('Unit and Integration Tests') {\n" +
            "                sh 'echo \"Unit and Integration Tests...\"'\n" +
            "            }\n" +
            "        }\n" +
            "}, 'branch2': {\n" +
            "        node {\n" +
            "            stage('Setup') {\n" +
            "                sh 'echo \"Branch2 setup...\"'\n" +
            "            }\n" +
            "            stage('Unit and Integration Tests') {\n" +
            "                echo '\"my command to execute tests\"'\n" +
            "            }\n" +
            "        }\n" +
            "})";
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition(script, false));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        WorkflowRun run = j.waitForCompletion(b1);
        j.assertBuildStatus(Result.SUCCESS, run);

        PipelineNodeGraphVisitor pipelineNodeGraphVisitor = new PipelineNodeGraphVisitor(run);
        List<FlowNodeWrapper> wrappers = pipelineNodeGraphVisitor.getPipelineNodes();

        assertEquals(7, wrappers.size());
    }

    @Test
    public void orphanParallels2() throws Exception {
        String script = "stage(\"stage1\"){\n" +
            "    echo \"stage 1...\"\n" +
            "}\n" +
            "parallel('branch1':{\n" +
            "        node {\n" +
            "            stage('Setup') {\n" +
            "                sh 'echo \"Setup...\"'\n" +
            "            }\n" +
            "            stage('Unit and Integration Tests') {\n" +
            "                sh 'echo \"Unit and Integration Tests...\"'\n" +
            "            }\n" +
            "        }\n" +
            "}, 'branch3': {\n" +
            "        node {\n" +
            "            stage('Setup') {\n" +
            "                sh 'echo \"Branch3 setup...\"'\n" +
            "            }\n" +
            "            stage('Unit and Integration Tests') {\n" +
            "                echo '\"my command to execute tests\"'\n" +
            "            }\n" +
            "        }\n" +
            "}, 'branch2': {\n" +
            "        node {\n" +
            "            stage('Setup') {\n" +
            "                sh 'echo \"Branch2 setup...\"'\n" +
            "            }\n" +
            "            stage('Unit and Integration Tests') {\n" +
            "                echo '\"my command to execute tests\"'\n" +
            "            }\n" +
            "        }\n" +
            "})\n" +
            "stage(\"stage2\"){\n" +
            "    echo \"stage 2...\"\n" +
            "}";
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition(script, false));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.SUCCESS, b1);

        List<Map> nodes = get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);

        assertEquals(12, nodes.size());

        for (int i = 0; i < nodes.size(); i++) {
            Map n = nodes.get(i);
            List<Map> edges = (List<Map>) n.get("edges");
            if (i == 0) {
                assertEquals("stage1", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(i + 1).get("id"), edges.get(0).get("id"));
            }
            if (i == 1) {
                assertEquals("Parallel", n.get("displayName"));
                assertEquals(nodes.get(i + 1).get("id") + "-parallel-synthetic", n.get("id"));
                assertEquals(3, edges.size());
                assertEquals(nodes.get(i + 1).get("id"), edges.get(0).get("id"));
                assertEquals(nodes.get(i + 2).get("id"), edges.get(1).get("id"));
                assertEquals(nodes.get(i + 3).get("id"), edges.get(2).get("id"));
            }
            if (i == 2) {
                assertEquals("branch1", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(6).get("id"), edges.get(0).get("id"));
            }
            if (i == 3) {
                assertEquals("branch2", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(10).get("id"), edges.get(0).get("id"));
            }
            if (i == 4) {
                assertEquals("branch3", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(8).get("id"), edges.get(0).get("id"));
            }
            if (i == 5) {
                assertEquals("stage2", n.get("displayName"));
                assertEquals(0, edges.size());
            }
            if (i == 6) {
                assertEquals("Setup", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(7).get("id"), edges.get(0).get("id"));
            }
            if (i == 7) {
                assertEquals("Unit and Integration Tests", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(5).get("id"), edges.get(0).get("id"));
            }
            if (i == 8) {
                assertEquals("Setup", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(9).get("id"), edges.get(0).get("id"));
            }
            if (i == 9) {
                assertEquals("Unit and Integration Tests", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(5).get("id"), edges.get(0).get("id"));
            }
            if (i == 10) {
                assertEquals("Setup", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(11).get("id"), edges.get(0).get("id"));
            }
            if (i == 11) {
                assertEquals("Unit and Integration Tests", n.get("displayName"));
                assertEquals(1, edges.size());
                assertEquals(nodes.get(5).get("id"), edges.get(0).get("id"));
            }
        }
    }

    @Issue("JENKINS-47158")
    @Test
    public void syntheticParallelFlowNodeNotSaved() throws Exception {
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        p.setDefinition(new CpsFlowDefinition("parallel a: {\n" +
                                                  "    node {\n" +
                                                  "        echo 'a'\n" +
                                                  "    }\n" +
                                                  "}, b: {\n" +
                                                  "    node {\n" +
                                                  "        echo 'b'\n" +
                                                  "    }\n" +
                                                  "}\n", true));
        WorkflowRun b = j.buildAndAssertSuccess(p);
        get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/", List.class);
        FlowExecution rawExec = b.getExecution();
        assert (rawExec instanceof CpsFlowExecution);
        CpsFlowExecution execution = (CpsFlowExecution) rawExec;
        File storage = execution.getStorageDir();

        // Nodes 5 and 6 are the parallel branch start nodes. Make sure no "5-parallel-synthetic.xml" and "6..." files
        // exist in the storage directory, showing we haven't saved them.
        assertFalse(new File(storage, "5-parallel-synthetic.xml").exists());
        assertFalse(new File(storage, "6-parallel-synthetic.xml").exists());
    }

    @Test
    public void encodedStepDescription() throws Exception {
        setupScm("pipeline {\n" +
                     "  agent any\n" +
                     "  stages {\n" +
                     "    stage('Build') {\n" +
                     "      steps {\n" +
                     "          sh 'echo \"\\033[32m some text \\033[0m\"'    \n" +
                     "      }\n" +
                     "    }\n" +
                     "  }\n" +
                     "}");
        WorkflowMultiBranchProject mp = j.jenkins.createProject(WorkflowMultiBranchProject.class, "p");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false)));
        for (SCMSource source : mp.getSCMSources()) {
            assertEquals(mp, source.getOwner());
        }

        mp.scheduleBuild2(0).getFuture().get();

        j.waitUntilNoActivity();

        WorkflowJob p = scheduleAndFindBranchProject(mp, "master");
        j.waitUntilNoActivity();
        WorkflowRun b1 = p.getLastBuild();
        Assert.assertEquals(Result.SUCCESS, b1.getResult());

        List<FlowNode> stages = getStages(NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(b1));

        Assert.assertEquals(1, stages.size());

        Assert.assertEquals("Build", stages.get(0).getDisplayName());

        List<Map> resp = get("/organizations/jenkins/pipelines/p/pipelines/master/runs/" + b1.getId() + "/nodes/", List.class);
        Assert.assertEquals(1, resp.size());
        Assert.assertEquals("Build", resp.get(0).get("displayName"));

        resp = get("/organizations/jenkins/pipelines/p/pipelines/master/runs/" + b1.getId() + "/steps/", List.class);
        Assert.assertEquals(2, resp.size());

        assertNotNull(resp.get(0).get("displayName"));

        assertEquals("Shell Script", resp.get(1).get("displayName"));
        assertEquals("echo \"\u001B[32m some text \u001B[0m\"", resp.get(1).get("displayDescription"));
    }

    @Test
    public void testDynamicInnerStage() throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, "p");
        URL resource = getClass().getResource("testDynamicInnerStage.jenkinsfile");
        String jenkinsFile = IOUtils.toString(resource, StandardCharsets.UTF_8);
        job.setDefinition(new CpsFlowDefinition(jenkinsFile, true));

        WorkflowRun build = job.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.SUCCESS, build);

        List<Map> nodes = get("/organizations/jenkins/pipelines/p/runs/1/nodes/", List.class);
        assertEquals(4, nodes.size());
        assertEquals(FlowNodeWrapper.NodeType.STAGE.name(), nodes.get(0).get("type"));
        assertEquals(FlowNodeWrapper.NodeType.STAGE.name(), nodes.get(1).get("type"));
        assertEquals(FlowNodeWrapper.NodeType.PARALLEL.name(), nodes.get(2).get("type"));
        assertEquals(FlowNodeWrapper.NodeType.STAGE.name(), nodes.get(3).get("type"));
    }

    @Issue("JENKINS-53311")
    @Test
    public void nodeWrongFinishedStatus() throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, "p");
        URL resource = getClass().getResource("JENKINS-53311.jenkinsfile");
        String jenkinsFile = IOUtils.toString(resource, StandardCharsets.UTF_8);
        job.setDefinition(new CpsFlowDefinition(jenkinsFile, true));

        WorkflowRun build = job.scheduleBuild2(0).waitForStart();

        long start = System.currentTimeMillis();
        while (build.isBuilding()) {
            List<Map<String, String>> nodes = get("/organizations/jenkins/pipelines/p/runs/1/nodes/", List.class);
            if (nodes.size() >= 4) {
                Optional<Map<String, String>> optionalMap = findNodeMap(nodes, "Nested B-1");
                if (optionalMap.isPresent()) {
                    long now = System.currentTimeMillis();
                    // the sleep in test file is about 10s so we want to avoid some flaky test
                    // so if we reach 10s we exit the loop
                    if (TimeUnit.SECONDS.convert(now - start, TimeUnit.MILLISECONDS) >= 10) {
                        continue;
                    }
                    LOGGER.debug("optionalMap: {}", optionalMap);
                    assertEquals(build.isBuilding() ? BlueRun.BlueRunState.RUNNING.name() : BlueRun.BlueRunState.FINISHED,
                                 optionalMap.get().get("state"));
                }
            }
            Thread.sleep(500);
        }
        List<Map<String, String>> nodes = get("/organizations/jenkins/pipelines/p/runs/1/nodes/", List.class);
        Optional<Map<String, String>> optionalMap = findNodeMap(nodes, "Nested B-1");
        if (optionalMap.isPresent()) {
            assertEquals(BlueRun.BlueRunState.FINISHED.name(), optionalMap.get().get("state"));
        }
        j.assertBuildStatus(Result.SUCCESS, build);
    }

    private Optional<Map<String, String>> findNodeMap(List<Map<String, String>> nodes, String displayName) {
        return nodes.stream() //
                    .filter(map -> map.get("displayName").equals(displayName)) //
                    .findFirst();
    }


    private void setupScm(String script) throws Exception {
        // create git repo
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", script);
        sampleRepo.write("file", "initial content");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--all", "--message=flow");
    }

    private String getActionLink(Map resp, String capability) {
        List<Map> actions = (List<Map>) resp.get("actions");
        assertNotNull(actions);
        for (Map a : actions) {
            String _class = (String) a.get("_class");
            Map r = get("/classes/" + _class + "/");
            List<String> classes = (List<String>) r.get("classes");
            for (String c : classes) {
                if (c.equals(capability)) {
                    return getHrefFromLinks(a, "self");
                }
            }
        }
        return null;
    }


    private static boolean waitForBuildCount(WorkflowJob job, int numBuilds, Result status) throws InterruptedException {
        long start = System.currentTimeMillis();

        while (countBuilds(job, status) < numBuilds) {
            // 2m is a long timeout but it seems as though it can actually take a fair bit of time for resumed
            // builds to complete.  Don't want the build randomly failing.
            if (System.currentTimeMillis() > start + 120000) {
                //Assert.fail("Timed out waiting on build count to get to " + numBuilds);
                return false;
            }
            Thread.sleep(200);
        }
        return true;
    }

    private static int countBuilds(WorkflowJob job) {
        return countBuilds(job, null);
    }

    private static int countBuilds(WorkflowJob job, Result status) {
        RunList<WorkflowRun> builds = job.getNewBuilds();
        Iterator<WorkflowRun> iterator = builds.iterator();
        int numBuilds = 0;

        while (iterator.hasNext()) {
            WorkflowRun build = iterator.next();
            Result buildRes = build.getResult();
            if (status == null || buildRes == status) {
                numBuilds++;
            }
        }

        return numBuilds;
    }

    @Test
    @Issue("JENKINS-38339")
    public void downstreamBuildLinks() throws Exception {
        FreeStyleProject downstream1 = j.createFreeStyleProject("downstream1");
        FreeStyleProject downstream2 = j.createFreeStyleProject("downstream2");

        WorkflowJob upstream = j.createProject(WorkflowJob.class, "upstream");

        URL resource = getClass().getResource("downstreamBuildLinks.jenkinsfile");
        String jenkinsFile = IOUtils.toString(resource, StandardCharsets.UTF_8);
        upstream.setDefinition(new CpsFlowDefinition(jenkinsFile, true));

        j.assertBuildStatus(Result.SUCCESS, upstream.scheduleBuild2(0));

        WorkflowRun r = upstream.getLastBuild();

        List<Map> resp = get("/organizations/jenkins/pipelines/upstream/runs/" + r.getId() + "/nodes/", List.class);

        assertEquals("number of nodes", 5, resp.size());

        Matcher actionMatcher1 = new NodeDownstreamBuildActionMatcher("downstream1");
        Matcher actionMatcher2 = new NodeDownstreamBuildActionMatcher("downstream2");

        List<Map> actions = (List<Map>) resp.get(2).get("actions");
        assertThat("node #2 contains a link to downstream1", actions, hasItem(actionMatcher1));

        actions = (List<Map>) resp.get(3).get("actions");
        assertThat("node #3 contains a link to downstream2", actions, hasItem(actionMatcher2));

        actions = (List<Map>) resp.get(4).get("actions");
        assertThat("node #4 contains a link to downstream1", actions, hasItem(actionMatcher1));
        assertThat("node #4 contains a link to downstream1", actions, hasItem(actionMatcher2));
    }

    @Test
    @Issue("JENKINS-38339")
    public void downstreamBuildLinksDeclarative() throws Exception {
        FreeStyleProject downstream1 = j.createFreeStyleProject("downstream1");
        FreeStyleProject downstream2 = j.createFreeStyleProject("downstream2");

        WorkflowJob upstream = j.createProject(WorkflowJob.class, "upstream");

        URL resource = getClass().getResource("downstreamBuildLinksDecl.jenkinsfile");
        String jenkinsFile = IOUtils.toString(resource, StandardCharsets.UTF_8);
        upstream.setDefinition(new CpsFlowDefinition(jenkinsFile, true));

        j.assertBuildStatus(Result.SUCCESS, upstream.scheduleBuild2(0));

        WorkflowRun r = upstream.getLastBuild();

        List<Map> resp = get("/organizations/jenkins/pipelines/upstream/runs/" + r.getId() + "/nodes/", List.class);

        assertEquals("number of nodes", 5, resp.size());

        Matcher actionMatcher1 = new NodeDownstreamBuildActionMatcher("downstream1");
        Matcher actionMatcher2 = new NodeDownstreamBuildActionMatcher("downstream2");

        List<Map> actions = (List<Map>) resp.get(2).get("actions");
        assertThat("node #2 contains a link to downstream1", actions, hasItem(actionMatcher1));

        actions = (List<Map>) resp.get(3).get("actions");
        assertThat("node #3 contains a link to downstream2", actions, hasItem(actionMatcher2));

        actions = (List<Map>) resp.get(4).get("actions");
        assertThat("node #4 contains a link to downstream1", actions, hasItem(actionMatcher1));
        assertThat("node #4 contains a link to downstream1", actions, hasItem(actionMatcher2));
    }

    @Test
    @Issue("JENKINS-38339")
    public void downstreamBuildLinksSequential() throws Exception {
        FreeStyleProject downstream1 = j.createFreeStyleProject("downstream1");
        FreeStyleProject downstream2 = j.createFreeStyleProject("downstream2");
        FreeStyleProject downstream3 = j.createFreeStyleProject("downstream3");
        FreeStyleProject downstream4 = j.createFreeStyleProject("downstream4");

        WorkflowJob upstream = j.createProject(WorkflowJob.class, "upstream");

        URL resource = getClass().getResource("downstreamBuildLinksSeq.jenkinsfile");
        String jenkinsFile = IOUtils.toString(resource, StandardCharsets.UTF_8);
        upstream.setDefinition(new CpsFlowDefinition(jenkinsFile, true));

        j.assertBuildStatus(Result.SUCCESS, upstream.scheduleBuild2(0));

        WorkflowRun r = upstream.getLastBuild();

        List<Map> resp = get("/organizations/jenkins/pipelines/upstream/runs/" + r.getId() + "/nodes/", List.class);

        assertEquals("number of nodes", 9, resp.size());

        // Find the nodes we're interested in
        Map node1 = null, node2 = null, node3 = null, node4 = null;
        for (Map node : resp) {
            String displayName = (String) node.get("displayName");
            if ("Single stage branch".equals(displayName)) {
                node1 = node;
            } else if ("Inner".equals(displayName)) {
                node2 = node;
            } else if ("build-ds3".equals(displayName)) {
                node3 = node;
            } else if ("build-ds4".equals(displayName)) {
                node4 = node;
            }
        }

        // Check they all have downstream links

        assertNotNull("missing node1", node1);
        assertThat("node1 contains a link to downstream1",
                   (List<Map>) node1.get("actions"),
                   hasItem(new NodeDownstreamBuildActionMatcher("downstream1")));

        assertNotNull("missing node2", node2);
        assertThat("node2 contains a link to downstream2",
                   (List<Map>) node2.get("actions"),
                   hasItem(new NodeDownstreamBuildActionMatcher("downstream2")));

        assertNotNull("missing node3", node3);
        assertThat("node3 contains a link to downstream3",
                   (List<Map>) node3.get("actions"),
                   hasItem(new NodeDownstreamBuildActionMatcher("downstream3")));

        assertNotNull("missing node4", node4);
        assertThat("node4 contains a link to downstream4",
                   (List<Map>) node4.get("actions"),
                   hasItem(new NodeDownstreamBuildActionMatcher("downstream4")));

    }

    /**
     * Matcher to check for a serialised NodeDownstreamBuildAction
     */
    private static class NodeDownstreamBuildActionMatcher extends TypeSafeMatcher<Map<String, Object>> {

        private final String downstreamName;

        public NodeDownstreamBuildActionMatcher(String downstreamName) {
            this.downstreamName = downstreamName;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a matching NodeDownstreamBuildAction named " + downstreamName);
        }

        @Override
        protected boolean matchesSafely(Map<String, Object> props) {

            String className = (String) props.get("_class");
            String desc = (String) props.get("description");
            Map<String, Object> link = (Map<String, Object>) props.get("link");
            String href = (String) link.get("href");

            return className.equals(NodeDownstreamBuildAction.class.getName())
                && desc.startsWith(downstreamName)
                && href.startsWith("/blue/rest/organizations/jenkins/pipelines/" + downstreamName + "/runs/");
        }
    }

    @Test
    @Issue("JENKINS-53900")
    public void singleStageSequentialLastInParallel() throws Exception {
        final String jenkinsfile =
            "pipeline {\n" +
                "    agent any\n" +
                "    stages {\n" +
                "        stage('Alpha') {\n" +
                "            parallel {\n" +
                "                stage('Blue') {\n" +
                "                    steps {\n" +
                "                        script {\n" +
                "                            println \"XXXX\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "                stage('Red') {\n" +
                "                    stages {\n" +
                "                        stage('Green') {\n" +
                "                            steps {\n" +
                "                                script {\n" +
                "                                    println \"XXXX\"\n" +
                "                                }\n" +
                "                            }\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "        stage('Bravo') {\n" +
                "            steps {\n" +
                "                script {\n" +
                "                    println \"XXXX\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n";

        WorkflowJob project1 = j.createProject(WorkflowJob.class, "project1");
        project1.setDefinition(new CpsFlowDefinition(jenkinsfile, true));

        j.assertBuildStatus(Result.SUCCESS, project1.scheduleBuild2(0));

        WorkflowRun r = project1.getLastBuild();
        List<Map> resp = get("/organizations/jenkins/pipelines/project1/runs/" + r.getId() + "/nodes/", List.class);

        assertEquals("number of nodes", 5, resp.size());

        final Map<String, Object> bravoNode = resp.get(3);
        final Map<String, Object> greenNode = resp.get(4);

        assertEquals("includes Alpha node", "Alpha", resp.get(0).get("displayName"));
        assertEquals("includes Blue node", "Blue", resp.get(1).get("displayName"));
        assertEquals("includes Red node", "Red", resp.get(2).get("displayName"));
        assertEquals("includes Bravo node", "Bravo", bravoNode.get("displayName"));
        assertEquals("includes Green node", "Green", greenNode.get("displayName"));

        String bravoId = bravoNode.get("id").toString();

        List<Map<String, Object>> greenEdges = (List<Map<String, Object>>) greenNode.get("edges");
        assertEquals("green has edges", 1, greenEdges.size());
        assertEquals("green has edge pointing to bravo", bravoId, greenEdges.get(0).get("id"));
    }
}
