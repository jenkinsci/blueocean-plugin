package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.tasks.Shell;
import io.jenkins.blueocean.commons.JsonConverter;
import org.hamcrest.Matchers;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.visualization.table.FlowGraphTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

/**
 * @author Vivek Pandey
 */
public class PipelineApiTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void before() {
        RestAssured.baseURI = j.jenkins.getRootUrl()+"blue/rest";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void getPipelineTest() throws IOException {
        Project p = j.createFreeStyleProject("pipeline1");

        RestAssured.given().log().all().get("/organizations/jenkins/pipelines/pipeline1")
            .then().log().all()
            .statusCode(200)
            .body("organization", Matchers.equalTo("jenkins"))
            .body("name", Matchers.equalTo("pipeline1"))
            .body("displayName", Matchers.equalTo("pipeline1"))
            .body("weatherScore", Matchers.is(p.getBuildHealth().getScore()));
    }

    /** TODO: latest stapler change broke delete, disabled for now */
//    @Test
    public void deletePipelineTest() throws IOException {
        Project p = j.createFreeStyleProject("pipeline1");

        RestAssured.given().log().all().delete("/organizations/jenkins/pipelines/pipeline1/")
            .then().log().all()
            .statusCode(200);

        Assert.assertNull(j.jenkins.getItem(p.getName()));
    }


    @Test
    public void getFreeStyleJobTest() throws IOException {
        Project p1 = j.createFreeStyleProject("pipeline1");
        Project p2 = j.createFreeStyleProject("pipeline2");
        RestAssured.given().log().all().get("/organizations/jenkins/pipelines/")
            .then().log().all()
            .statusCode(200)
            .body("[0].organization", Matchers.equalTo("jenkins"))
            .body("[0].name", Matchers.equalTo(p1.getName()))
            .body("[0].displayName", Matchers.equalTo(p1.getDisplayName()))
            .body("[0].weatherScore", Matchers.is(p1.getBuildHealth().getScore()))
            .body("[1].organization", Matchers.equalTo("jenkins"))
            .body("[1].name", Matchers.equalTo(p2.getName()))
            .body("[1].displayName", Matchers.equalTo(p2.getDisplayName()))
            .body("[1].weatherScore", Matchers.is(p1.getBuildHealth().getScore()));
    }



    @Test
    public void findPipelinesTest() throws IOException {
        FreeStyleProject p1 = j.createFreeStyleProject("pipeline2");
        FreeStyleProject p2 = j.createFreeStyleProject("pipeline3");

        RestAssured.given().log().all().get("/search?q=type:pipeline;organization:jenkins")
            .then().log().all()
            .statusCode(200)
            .body("[0].organization", Matchers.equalTo("jenkins"))
            .body("[0].name", Matchers.equalTo(p1.getName()))
            .body("[1].organization", Matchers.equalTo("jenkins"))
            .body("[1].name", Matchers.equalTo(p2.getName()));
    }

    @Test
    public void getPipelineRunTest() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline4");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);
        RestAssured.given().log().all().get("/organizations/jenkins/pipelines/pipeline4/runs/"+b.getId())
            .then().log().all()
            .statusCode(200)
            .body("id", Matchers.equalTo(b.getId()))
            .body("pipeline", Matchers.equalTo(p.getName()))
            .body("pipeline", Matchers.equalTo(p.getName()))
            .body("organization", Matchers.equalTo("jenkins"))
            .body("startTime", Matchers.equalTo(
                new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b.getStartTimeInMillis()))));
    }

    @Test
    public void getPipelineRunLatestTest() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline5");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);

        RestAssured.given().log().all().get("/search?q=type:run;organization:jenkins;pipeline:pipeline5;latestOnly:true")
            .then().log().all()
            .statusCode(200)
            .body("[0].id", Matchers.equalTo(b.getId()))
            .body("[0].pipeline", Matchers.equalTo(p.getName()))
            .body("[0].organization", Matchers.equalTo("jenkins"))
            .body("[0].startTime", Matchers.equalTo(
                new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b.getStartTimeInMillis()))));
    }

    @Test
    public void getPipelineRunsTest() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline6");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);
        System.out.println(new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b.getStartTimeInMillis())));
        RestAssured.given().log().all().get("/organizations/jenkins/pipelines/pipeline6/runs")
            .then().log().all()
            .statusCode(200)
            .body("[0].id", Matchers.equalTo(b.getId()))
            .body("[0].pipeline", Matchers.equalTo(p.getName()))
            .body("[0].organization", Matchers.equalTo("jenkins"))
            .body("[0].startTime", Matchers.equalTo(
                new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b.getStartTimeInMillis()))));
    }


    @Test
    public void getPipelineJobsTest() throws IOException {
        WorkflowJob p1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        WorkflowJob p2 = j.jenkins.createProject(WorkflowJob.class, "pipeline2");
        RestAssured.given().log().all().get("/organizations/jenkins/pipelines/")
            .then().log().all()
            .statusCode(200)
            .body("[0].organization", Matchers.equalTo("jenkins"))
            .body("[0].name", Matchers.equalTo(p1.getName()))
            .body("[0].displayName", Matchers.equalTo(p1.getDisplayName()))
            .body("[1].organization", Matchers.equalTo("jenkins"))
            .body("[1].name", Matchers.equalTo(p2.getName()))
            .body("[1].displayName", Matchers.equalTo(p2.getDisplayName()));
    }

    @Test
    public void getPipelineJobRunTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        job1.setDefinition(new CpsFlowDefinition("" +
            "node {" +
            "   stage ('Build1'); " +
            "   echo ('Building'); " +
            "   stage ('Test1'); " +
            "   echo ('Testing'); " +
            "}"));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        RestAssured.given().log().all().get("/organizations/jenkins/pipelines/pipeline1/runs/1/stages/")
            .then().log().all()
            .statusCode(200)
            .body("id", Matchers.equalTo(b1.getId()))
            .body("pipeline", Matchers.equalTo(b1.getParent().getName()))
            .body("organization", Matchers.equalTo("jenkins"))
            .body("state", Matchers.equalTo("FINISHED"))
            .body("result", Matchers.equalTo("SUCCESS"))
            .body("startTime", Matchers.equalTo(
                new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b1.getStartTimeInMillis()))));
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
            "}"));
        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        FlowGraphTable nodeGraphTable = new FlowGraphTable(b1.getExecution());
        nodeGraphTable.build();
        List<FlowNode> nodes = getStages(nodeGraphTable);
        List<FlowNode> parallelNodes = getParallelNodes(nodeGraphTable);

        for(FlowGraphTable.Row row: nodeGraphTable.getRows()){
            if(row.getNode().getAction(StageAction.class) != null || row.getNode().getAction(ThreadNameAction.class) != null){
                nodes.add(row.getNode());
            }
            if(row.getNode().getAction(ThreadNameAction.class) != null){
                parallelNodes.add(row.getNode());
            }

        }


        Assert.assertEquals(6, nodes.size());
        Assert.assertEquals(3, parallelNodes.size());
        ValidatableResponse response = RestAssured.given().log().all().get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/")
            .then().log().all()
            .statusCode(200);

        response.body("size()", Matchers.is(nodes.size()));

        for(int i=0; i< nodes.size();i++){
            FlowNode n = nodes.get(i);
            response.body(String.format("[%s].id", i), Matchers.equalTo(n.getId()))
                .body(String.format("[%s].displayName", i),
                    Matchers.equalTo(n.getAction(ThreadNameAction.class) != null
                    ? n.getAction(ThreadNameAction.class).getThreadName()
                    : n.getDisplayName()));
            if(n.getDisplayName().equals("test")){
                response.body(String.format("[%s].edges.size()", i), Matchers.is(parallelNodes.size()));
                response.body(String.format("[%s].edges[0].id", i), Matchers.equalTo(parallelNodes.get(0).getId()))
                .body(String.format("[%s].edges[1].id", i), Matchers.equalTo(parallelNodes.get(1).getId()))
                .body(String.format("[%s].edges[2].id", i), Matchers.equalTo(parallelNodes.get(2).getId()));
            }else if(n.getDisplayName().equals("build")){
                response.body(String.format("[%s].edges.size()", i), Matchers.is(1));
                response.body(String.format("[%s].edges[0].id", i), Matchers.equalTo(nodes.get(i+1).getId()));
            }else if(n.getDisplayName().equals("deploy")){
                response.body(String.format("[%s].edges", i), Matchers.isEmptyOrNullString());
            }else{
                response.body(String.format("[%s].edges.size()", i), Matchers.is(1));
                response.body(String.format("[%s].edges[0].id", i), Matchers.equalTo(nodes.get(nodes.size() - 1).getId()));
            }
        }
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


        RestAssured.given().log().all().get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/")
            .then().log().all()
            .statusCode(200);


        FlowNode n = nodes.get(0);

        RestAssured.given().log().all()
            .get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/"+nodes.get(0).getId())
            .then().log().all()
            .statusCode(200)
            .body("id", Matchers.equalTo(n.getId()))
            .body("displayName",
                Matchers.equalTo(n.getAction(ThreadNameAction.class) != null
                    ? n.getAction(ThreadNameAction.class).getThreadName()
                    : n.getDisplayName()))
            .body("edges.size()", Matchers.is(1))
            .body("edges[0].id", Matchers.equalTo(nodes.get(1).getId()));


        n = parallelNodes.get(0);
        RestAssured.given().log().all()
            .get("/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/"+parallelNodes.get(0).getId())
            .then().log().all()
            .statusCode(200)
            .body("id", Matchers.equalTo(n.getId()))
            .body("displayName",
                Matchers.equalTo(n.getAction(ThreadNameAction.class) != null
                    ? n.getAction(ThreadNameAction.class).getThreadName()
                    : n.getDisplayName()))
            .body("edges.size()", Matchers.is(1))
            .body("edges[0].id", Matchers.equalTo(nodes.get(nodes.size()-1).getId()));

    }


    @Test
    public void getPipelineJobRunsTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
//        WorkflowJob job2 = j.jenkins.createProject(WorkflowJob.class, "pipeline2");

        job1.setDefinition(new CpsFlowDefinition("" +
            "node {" +
            "   stage ('Build1'); " +
            "   echo ('Building'); " +
            "   stage ('Test1'); " +
            "   echo ('Testing'); " +
            "}"));

//        job2.setDefinition(new CpsFlowDefinition("" +
//            "node {" +
//            "   stage ('Build1'); " +
//            "   echo ('Building'); " +
//            "   stage ('Test1'); " +
//            "   echo ('Testing'); " +
//            "}"));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        WorkflowRun b2 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b2);

        RestAssured.given().log().all().get("/organizations/jenkins/pipelines/pipeline1/runs")
            .then().log().all()
            .statusCode(200)
            .body("[0].id", Matchers.equalTo(b2.getId()))
            .body("[0].pipeline", Matchers.equalTo(b2.getParent().getName()))
            .body("[0].organization", Matchers.equalTo("jenkins"))
            .body("[0].startTime", Matchers.equalTo(
                new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b2.getStartTimeInMillis()))))
            .body("[1].id", Matchers.equalTo(b1.getId()))
            .body("[1].pipeline", Matchers.equalTo(b1.getParent().getName()))
            .body("[1].organization", Matchers.equalTo("jenkins"))
            .body("[1].startTime", Matchers.equalTo(
                new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b1.getStartTimeInMillis()))));
    }

    @Test
    public void getPipelineJobRunsLogTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition("" +
            "node {" +
            "   stage ('Build1'); " +
            "   echo ('Building'); " +
            "   stage ('Test1'); " +
            "   echo ('Testing'); " +
            "}"));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        Response response = RestAssured.given().log().all()
            .accept(ContentType.TEXT)
            .get("/organizations/jenkins/pipelines/pipeline1/runs/"+b1.getId()+"/log?start=0");

            response.then().log().all()
            .statusCode(200);
        int size = Integer.parseInt(response.getHeader("X-Text-Size"));
        Assert.assertTrue(size > 0);
    }

    @Test
    public void findPipelineRunsForAPipelineTest() throws Exception {
        FreeStyleProject p1 = j.createFreeStyleProject("pipeline1");
        FreeStyleProject p2 = j.createFreeStyleProject("pipeline2");
        p1.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        p2.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        Stack<FreeStyleBuild> builds = new Stack<FreeStyleBuild>();
        FreeStyleBuild b11 = p1.scheduleBuild2(0).get();
        FreeStyleBuild b12 = p1.scheduleBuild2(0).get();
        builds.push(b11);
        builds.push(b12);

        j.assertBuildStatusSuccess(b11);
        j.assertBuildStatusSuccess(b12);

        ValidatableResponse response = RestAssured.given().log().all().get("/search?q=type:run;organization:jenkins;pipeline:pipeline1")
            .then().log().all()
            .statusCode(200);

        for (int i = 0; i < builds.size(); i++) {
            FreeStyleBuild b = builds.pop();
            response.body(String.format("[%s].id",i), Matchers.equalTo(b.getId()))
                .body(String.format("[%s].pipeline",i), Matchers.equalTo(b.getParent().getName()))
                .body(String.format("[%s].organization",i), Matchers.equalTo("jenkins"))
                .body(String.format("[%s].startTime",i), Matchers.equalTo(
                    new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b.getStartTimeInMillis()))));
        }
    }

    @Test
    public void findPipelineRunsForAllPipelineTest() throws IOException, ExecutionException, InterruptedException {
        FreeStyleProject p1 = j.createFreeStyleProject("pipeline11");
        FreeStyleProject p2 = j.createFreeStyleProject("pipeline22");
        p1.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        p2.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        Stack<FreeStyleBuild> p1builds = new Stack<FreeStyleBuild>();
        p1builds.push(p1.scheduleBuild2(0).get());
        p1builds.push(p1.scheduleBuild2(0).get());

        Stack<FreeStyleBuild> p2builds = new Stack<FreeStyleBuild>();
        p2builds.push(p2.scheduleBuild2(0).get());
        p2builds.push(p2.scheduleBuild2(0).get());

        Map<String, Stack<FreeStyleBuild>> buildMap = ImmutableMap.of(p1.getName(), p1builds, p2.getName(), p2builds);

        Response r = RestAssured.given().log().all().get("/search?q=type:run;organization:jenkins");
        ValidatableResponse response = r
            .then().log().all()
            .statusCode(200);

        for (int i = 0; i < 4; i++) {
            String pipeline = r.path(String.format("[%s].pipeline",i));
            FreeStyleBuild b = buildMap.get(pipeline).pop();
            response.body(String.format("[%s].id",i), Matchers.equalTo(b.getId()))
                .body(String.format("[%s].pipeline",i), Matchers.equalTo(pipeline))
                .body(String.format("[%s].organization",i), Matchers.equalTo("jenkins"))
                .body(String.format("[%s].startTime",i), Matchers.equalTo(
                    new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b.getStartTimeInMillis()))));
        }
    }

    private List<FlowNode> getStages(FlowGraphTable nodeGraphTable){
        List<FlowNode> nodes = new ArrayList<>();
        for(FlowGraphTable.Row row: nodeGraphTable.getRows()){
            if(row.getNode().getAction(StageAction.class) != null || row.getNode().getAction(ThreadNameAction.class) != null){
                nodes.add(row.getNode());
            }
        }
        return nodes;
    }

    private List<FlowNode> getParallelNodes(FlowGraphTable nodeGraphTable){
        List<FlowNode> parallelNodes = new ArrayList<>();

        for(FlowGraphTable.Row row: nodeGraphTable.getRows()){
            if(row.getNode().getAction(ThreadNameAction.class) != null){
                parallelNodes.add(row.getNode());
            }
        }
        return parallelNodes;
    }

}
