package io.jenkins.blueocean.rest.impl.pipeline;

import com.mashape.unirest.http.HttpResponse;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.Shell;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.stapler.AcceptHeader;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class PipelineApiTest extends PipelineBaseTest {


    @Test
    public void getPipelineRunStopTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        job1.setDefinition(new CpsFlowDefinition("" +
            "node {" +
            "   stage ('Build1'); " +
            "   sh('sleep 60') " +
            "   stage ('Test1'); " +
            "   echo ('Testing'); " +
            "}"));

        WorkflowRun b1 = job1.scheduleBuild2(0).waitForStart();
        Map r=null;

        for (int i = 0; i < 10; i++) {
             r = request().put("/organizations/jenkins/pipelines/pipeline1/runs/1/stop")
                .build(Map.class);
             if(((String) r.get("state")).equalsIgnoreCase("FINISHED"))
                continue;
            Thread.sleep(1000);
        }
        Assert.assertEquals(r.get("state"), "FINISHED");
        Assert.assertEquals(r.get("result"), "ABORTED");

        j.assertBuildStatus(Result.ABORTED, b1);

        FreeStyleProject p = j.createFreeStyleProject("pipeline5");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 69"));
        FreeStyleBuild b2 = p.scheduleBuild2(0).waitForStart();

        for (int i = 0; i < 10; i++) {
            r = put("/organizations/jenkins/pipelines/pipeline5/runs/1/stop",null);
            if(((String) r.get("state")).equalsIgnoreCase("finished"))
                continue;
            Thread.sleep(1000);
        }
        Assert.assertEquals(r.get("state"), "FINISHED");
        Assert.assertEquals(r.get("result"), "ABORTED");
        j.assertBuildStatus(Result.ABORTED, b2);

    }


    @Test
    public void getPipelineRunblockingStopTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        job1.setDefinition(new CpsFlowDefinition("" +
            "node {" +
            "   stage ('Build1'); " +
            "   sh('sleep 60') " +
            "   stage ('Test1'); " +
            "   echo ('Testing'); " +
            "}"));

        WorkflowRun b1 = job1.scheduleBuild2(0).waitForStart();

        Map r = request().put("/organizations/jenkins/pipelines/pipeline1/runs/1/stop/?blocking=true") //default timeOutInSecs=10 sec
            .build(Map.class);

        Assert.assertEquals(r.get("state"), "FINISHED");
        Assert.assertEquals(r.get("result"), "ABORTED");

        j.assertBuildStatus(Result.ABORTED, b1);

    }



    @Test
    public void getPipelineJobsTest() throws IOException {
        WorkflowJob p1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        WorkflowJob p2 = j.jenkins.createProject(WorkflowJob.class, "pipeline2");

        List<Map> resp = get("/organizations/jenkins/pipelines/", List.class);

        WorkflowJob[] projects = {p1,p2};

        Assert.assertEquals(projects.length, resp.size());

        for(int i=0; i<projects.length; i++){
            Map lr = resp.get(i);
            validatePipeline(projects[i], lr);
        }
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

        Map resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1");
        validateRun(b1, resp);
    }

    @Test
    public void getPipelineJobAbortTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        job1.setDefinition(new CpsFlowDefinition("" +
            "node {" +
            "   stage ('Build1'); " +
            "   sh('sleep 60') " +
            "   stage ('Test1'); " +
            "   echo ('Testing'); " +
            "}"));

        WorkflowRun b1 = job1.scheduleBuild2(0).waitForStart();
        for (int i = 0; i < 10; i++) {
            b1.doStop();
            if (b1.getResult() != null) {
                break;
            }
            Thread.sleep(1000);
        }
        j.assertBuildStatus(Result.ABORTED, b1);

        Map r = get("/organizations/jenkins/pipelines/pipeline1/runs/1");

        validateRun(b1, r);
    }

    @Test
    public void getPipelineJobRunsTest() throws Exception {
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

        WorkflowRun b2 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b2);

        Run[] runs = {b2,b1};

        List<Map> runResponses = get("/organizations/jenkins/pipelines/pipeline1/runs", List.class);

        for(int i=0; i < runs.length; i++){
            validateRun(runs[i], runResponses.get(i));
        };
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

        HttpResponse<String> response = get("/organizations/jenkins/pipelines/pipeline1/runs/"+b1.getId()+"/log?start=0", 200,HttpResponse.class);
        AcceptHeader acceptHeader = new AcceptHeader(response.getHeaders().getFirst("Content-Type"));
        Assert.assertNotNull(acceptHeader.select("text/plain"));

        int size = Integer.parseInt(response.getHeaders().getFirst("X-Text-Size"));
        System.out.println(response.getBody());
        Assert.assertTrue(size > 0);
    }

    @Test
    public void getPipelineJobActivities() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition("" +
            "node {" +
            "   stage ('Build1'); " +
            "   echo ('Building'); " +
            "   stage ('Test1'); " +
            "   sleep 10000      " +
            "   echo ('Testing'); " +
            "}"));

        job1.setConcurrentBuild(false);

        WorkflowRun r = job1.scheduleBuild2(0).waitForStart();
        job1.scheduleBuild2(0);


        List l = request().get("/organizations/jenkins/pipelines/pipeline1/activities").build(List.class);

        Assert.assertEquals(2, l.size());
        Assert.assertEquals("io.jenkins.blueocean.service.embedded.rest.QueueItemImpl", ((Map) l.get(0)).get("_class"));
        Assert.assertEquals("io.jenkins.blueocean.rest.impl.pipeline.PipelineRunImpl", ((Map) l.get(1)).get("_class"));
    }

}
