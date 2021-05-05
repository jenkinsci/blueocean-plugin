package io.jenkins.blueocean.rest.impl.pipeline;

import com.mashape.unirest.http.HttpResponse;
import hudson.matrix.Axis;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.Action;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.Shell;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.test.steps.SemaphoreStep;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.kohsuke.stapler.AcceptHeader;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

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
            "   stage ('Build'); " +
            "   semaphore 's' " +
            "   stage ('Test'); " +
            "   echo ('Testing'); " +
            "}", false));

        WorkflowRun b1 = job1.scheduleBuild2(0).waitForStart();
        SemaphoreStep.waitForStart("s/1", b1);
        Map r = null;
        for (int i = 0; i < 10; i++) {
             r = request().put("/organizations/jenkins/pipelines/pipeline1/runs/1/stop")
                .build(Map.class);
             if(((String) r.get("state")).equals("FINISHED"))
                break;
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
            if(((String) r.get("state")).equals("FINISHED"))
                break;
            Thread.sleep(1000);
        }
        Assert.assertEquals(r.get("state"), "FINISHED");
        Assert.assertEquals(r.get("result"), "ABORTED");
        j.assertBuildStatus(Result.ABORTED, b2);

    }

    //TODO: Fix test - see JENKINS-38319
    @Test @Ignore
    public void getPipelineRunblockingStopTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        job1.setDefinition(new CpsFlowDefinition("" +
            "node {" +
            "   stage ('Build1'); " +
            "   sleep(60);" +
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
            "   sleep(120);" +
            "   stage ('Test1'); " +
            "   echo ('Testing'); " +
            "}"));

        WorkflowRun b1 = job1.scheduleBuild2(0).waitForStart();
        // Give the job some time to really start (allocate nodes and so on)
        Thread.sleep(5000);
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

        HttpResponse<String> response = get("/organizations/jenkins/pipelines/pipeline1/runs/"+b1.getId()+"/log?start=0", 200,"text/html",HttpResponse.class);
        AcceptHeader acceptHeader = new AcceptHeader(response.getHeaders().getFirst("Content-Type"));
        Assert.assertNotNull(acceptHeader.select("text/plain"));

        int size = Integer.parseInt(response.getHeaders().getFirst("X-Text-Size"));
        System.out.println(response.getBody());
        Assert.assertTrue(size > 0);
    }

    @Test
    public void getPipelineJobRuns() throws Exception {
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


        List l = request().get("/organizations/jenkins/pipelines/pipeline1/runs").build(List.class);

        Assert.assertEquals(2, l.size());
        Assert.assertEquals("io.jenkins.blueocean.service.embedded.rest.QueuedBlueRun", ((Map) l.get(0)).get("_class"));
        Assert.assertEquals("io.jenkins.blueocean.rest.impl.pipeline.PipelineRunImpl", ((Map) l.get(1)).get("_class"));
    }

    @Test
    public void pipelineJobCapabilityTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");

        job1.setDefinition(new CpsFlowDefinition("" +
            "node {" +
            "   stage ('Build1'); " +
            "   sleep(60); " +
            "   stage ('Test1'); " +
            "   echo ('Testing'); " +
            "}"));

        Map response = get("/organizations/jenkins/pipelines/pipeline1/");

        String clazz = (String) response.get("_class");

        response = get("/classes/"+clazz+"/");
        Assert.assertNotNull(response);

        List<String> classes = (List<String>) response.get("classes");
        Assert.assertTrue(classes.contains("hudson.model.Job")
            && classes.contains("org.jenkinsci.plugins.workflow.job.WorkflowJob")
            && classes.contains("io.jenkins.blueocean.rest.model.BluePipeline")
            && !classes.contains("io.jenkins.blueocean.rest.model.BlueBranch")
        );
    }

    @Test
    public void matrixProjectTest() throws Exception{
        MatrixProject mp = j.jenkins.createProject(MatrixProject.class, "mp1");
        mp.getAxes().add(new Axis("os", "linux", "windows"));
        mp.getAxes().add(new Axis("jdk", "1.7", "1.8"));

        MatrixBuild matrixBuild = mp.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(matrixBuild);

        List<Map> pipelines = get("/search/?q=type:pipeline;excludedFromFlattening:jenkins.branch.MultiBranchProject,hudson.matrix.MatrixProject", List.class);
        Assert.assertEquals(1, pipelines.size());

        Map p = pipelines.get(0);

        Assert.assertEquals("mp1", p.get("name"));

        String href = getHrefFromLinks(p, "self");

        Assert.assertEquals("/job/mp1/", href);
    }

    @Issue("JENKINS-52307")
    @Test
    public void matrixProjectEmptyBuild() throws Exception{
        MatrixProject mp = j.jenkins.createProject(MatrixProject.class, "mp1");
        List response = get("/organizations/jenkins/pipelines/", List.class);
        Assert.assertNotNull( response );
    }

    @ExportedBean
    public static class TestAction implements Action {
        @Override
        public String getIconFileName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return "Test action";
        }

        @Override
        public String getUrlName() {
            return "bo-test";
        }

        @Exported
        public String getTest(){
            return "bo test works";
        }

        @Exported(name = "notExportableBean")
        public NotExportableBean getNotExportableBean(){
            return new NotExportableBean();
        }

        public static class NotExportableBean{

            @Exported
            public String getDisplayName() {
                return "Exploding action without ExportedBean";
            }

            @Exported
            public String getUrlName() {
                return "bo-test-without-exportedbean";
            }

            @Exported
            public String getTest(){
                throw new RuntimeException("failing");
            }
        }
    }

    @ExportedBean
    public static class ExplodingAction implements Action {
        @Override
        public String getIconFileName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return "Exploding action";
        }

        @Override
        public String getUrlName() {
            return "exploding-action";
        }

        @Exported
        public String getTest(){
            throw new RuntimeException("failing action");
        }

    }

    @Test
    public void getPipelineFailingActionTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        WorkflowJob job2 = j.jenkins.createProject(WorkflowJob.class, "pipeline2");
        String script = "\n" +
                "node {\n" +
                "   stage ('Build'){ \n" +
                "     echo ('Building'); \n" +
                "   } \n" +
                "   stage ('Test') { \n" +
                "     echo ('Testing'); \n" +
                "   } \n" +
                "}";
        job1.setDefinition(new CpsFlowDefinition(script));

        job1.addAction(new ExplodingAction());
        job1.addAction(new TestAction());

        job2.setDefinition(new CpsFlowDefinition(script));

        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        b1.addAction(new ExplodingAction());
        b1.addAction(new TestAction());


        WorkflowRun b2 = job2.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b2);

        List<Map> pipelines = get("/organizations/jenkins/pipelines/?tree=*[*]", List.class);
        Assert.assertEquals(2, pipelines.size());

        validateBrokenAction((List<Map>) pipelines.get(0).get("actions"));

        Map pipeline = get("/organizations/jenkins/pipelines/pipeline1/?tree=*[*]");
        validatePipeline(job1, pipeline);
        validateBrokenAction((List<Map>) pipeline.get("actions"));

        List<Map> runs = get("/organizations/jenkins/pipelines/pipeline1/runs/?tree=*[*]", List.class);
        Assert.assertEquals(1, runs.size());
        validateBrokenAction((List<Map>) pipelines.get(0).get("actions"));

        Map resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/?tree=*[*]");
        validateBrokenAction((List<Map>) resp.get("actions"));
        validateRun(b1, resp);
    }

    private void validateBrokenAction(List<Map> actions){
        boolean testActionFound = false;
        boolean explodingActionFound = false;
        for(Map a:actions){
            if(a.get("urlName") != null && a.get("urlName").equals("bo-test")){
                Assert.assertTrue(((Map)a.get("notExportableBean")).isEmpty());
                testActionFound = true;
                continue;
            }

            if(a.get("urlName") != null && a.get("urlName").equals("exploding-action")){
                Assert.assertTrue(a.get("urlName") != null && a.get("urlName").equals("exploding-action"));
                Assert.assertNull(a.get("test"));
                explodingActionFound = true;
            }
        }
        Assert.assertTrue(testActionFound);
        Assert.assertTrue(explodingActionFound);
    }
}
