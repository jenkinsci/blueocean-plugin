package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Shell;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

/**
 * @author Vivek Pandey
 */
public class PipelineApiTest extends BaseTest {

    @Test
    public void getFolderPipelineTest() throws IOException {
        MockFolder folder = j.createFolder("folder1");
        Project p = folder.createProject(FreeStyleProject.class, "test1");

        Map response = get("/organizations/jenkins/pipelines/test1");
        validatePipeline(p, response);
    }


    @Test
    public void getPipelineTest() throws IOException {
        Project p = j.createFreeStyleProject("pipeline1");

        Map<String,Object> response = get("/organizations/jenkins/pipelines/pipeline1");
        validatePipeline(p, response);
    }

    /** TODO: latest stapler change broke delete, disabled for now */
//    @Test
    public void deletePipelineTest() throws IOException {
        Project p = j.createFreeStyleProject("pipeline1");

        delete("/organizations/jenkins/pipelines/pipeline1/");

        Assert.assertNull(j.jenkins.getItem(p.getName()));
    }


    @Test
    public void getFreeStyleJobTest() throws Exception {
        Project p1 = j.createFreeStyleProject("pipeline1");
        Project p2 = j.createFreeStyleProject("pipeline2");
        p1.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = (FreeStyleBuild) p1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);

        List<Map> resp = get("/organizations/jenkins/pipelines/", List.class);
        Project[] projects = {p1,p2};

        Assert.assertEquals(projects.length, resp.size());

        for(int i=0; i<projects.length; i++){
            Map p = resp.get(i);
            validatePipeline(projects[i], p);
        }
    }



    @Test
    public void findPipelinesTest() throws IOException {
        FreeStyleProject p1 = j.createFreeStyleProject("pipeline2");
        FreeStyleProject p2 = j.createFreeStyleProject("pipeline3");

        List<Map> resp = get("/search?q=type:pipeline;organization:jenkins", List.class);
        Project[] projects = {p1,p2};

        Assert.assertEquals(projects.length, resp.size());

        for(int i=0; i<projects.length; i++){
            Map p = resp.get(i);
            validatePipeline(projects[i], p);
        }
    }

    @Test
    public void getPipelineWithLastSuccessfulRun() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline4");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);
        Map resp = get("/organizations/jenkins/pipelines/pipeline4/");

        validatePipeline(p, resp);
    }

    @Test
    public void getPipelineRunTest() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline4");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);
        Map resp = get("/organizations/jenkins/pipelines/pipeline4/runs/"+b.getId());
        validateRun(b,resp);
    }

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
    public void getPipelineRunLatestTest() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline5");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);

        List<Map> resp = get("/search?q=type:run;organization:jenkins;pipeline:pipeline5;latestOnly:true", List.class);
        Run[] run = {b};

        Assert.assertEquals(run.length, resp.size());

        for(int i=0; i<run.length; i++){
            Map lr = resp.get(i);
            validateRun(run[i], lr);
        }
    }

    @Test
    public void getPipelineRunsTest() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline6");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);

        List<Map> resp = get("/organizations/jenkins/pipelines/pipeline6/runs", List.class);
        Assert.assertEquals(1, resp.size());

        Map lr = resp.get(0);
        validateRun(b, lr);
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

        HttpResponse<String> response = get("/organizations/jenkins/pipelines/pipeline1/runs/"+b1.getId()+"/log?start=0", 200,"text/plain",HttpResponse.class);

        int size = Integer.parseInt(response.getHeaders().getFirst("X-Text-Size"));
        System.out.println(response.getBody());
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

        List<Map> resp = get("/search?q=type:run;organization:jenkins;pipeline:pipeline1", List.class);

        Assert.assertEquals(builds.size(), resp.size());
        for(int i=0; i< builds.size(); i++){
            Map p = resp.get(i);
            FreeStyleBuild b = builds.pop();
            validateRun(b, p);
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

        List<Map> resp = get("/search?q=type:run;organization:jenkins", List.class);

        Assert.assertEquals(4, resp.size());
        for(int i=0; i< 4; i++){
            Map p = resp.get(i);
            String pipeline = (String) p.get("pipeline");
            Assert.assertNotNull(pipeline);
            validateRun(buildMap.get(pipeline).pop(), p);
        }
    }

    @Test
    public void testArtifactsRunApi() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline1");
        p.getBuildersList().add(new TestBuilder() {
            @Override public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                FilePath ws = build.getWorkspace();
                if (ws == null) {
                    return false;
                }
                FilePath dir = ws.child("dir");
                dir.mkdirs();
                dir.child("fizz").write("contents", null);
                dir.child("lodge").symlinkTo("fizz", listener);
                return true;
            }
        });
        ArtifactArchiver aa = new ArtifactArchiver("dir/fizz");
        aa.setAllowEmptyArchive(true);
        p.getPublishersList().add(aa);
        FreeStyleBuild b = j.assertBuildStatusSuccess(p.scheduleBuild2(0));


        Map run = get("/organizations/jenkins/pipelines/pipeline1/runs/"+b.getId());

        validateRun(b, run);
        List<Map> artifacts = (List<Map>) run.get("artifacts");
        Assert.assertEquals(1, artifacts.size());
        Assert.assertEquals("fizz", artifacts.get(0).get("name"));
    }

}
