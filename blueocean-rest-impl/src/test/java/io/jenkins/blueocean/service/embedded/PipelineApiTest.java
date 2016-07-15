package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.StringParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Shell;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.junit.TestResultAction;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.BluePipelineFactory;
import io.jenkins.blueocean.service.embedded.rest.PipelineImpl;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.TestBuilder;
import org.kohsuke.stapler.AcceptHeader;
import org.kohsuke.stapler.export.Exported;

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

        Map response = get("/organizations/jenkins/pipelines/folder1/test1");
        validatePipeline(p, response);
    }


    @Test
    public void getNestedFolderPipelineTest() throws IOException {
        MockFolder folder1 = j.createFolder("folder1");
        Project p1 = folder1.createProject(FreeStyleProject.class, "test1");
        MockFolder folder2 = folder1.createProject(MockFolder.class, "folder2");
        MockFolder folder3 = folder1.createProject(MockFolder.class, "folder3");
        Project p2 = folder2.createProject(FreeStyleProject.class, "test2");

        List<Map> topFolders = get("/organizations/jenkins/pipelines/", List.class);

        Assert.assertEquals(1, topFolders.size());

        Map response = get("/organizations/jenkins/pipelines/folder1/pipelines/folder2/test2");
        validatePipeline(p2, response);

        List<Map> pipelines = get("/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/", List.class);
        Assert.assertEquals(1, pipelines.size());
        validatePipeline(p2, pipelines.get(0));

        pipelines = get("/organizations/jenkins/pipelines/folder1/pipelines/", List.class);
        Assert.assertEquals(3, pipelines.size());
        Assert.assertEquals("folder2", pipelines.get(0).get("name"));
        Assert.assertEquals("folder1/folder2", pipelines.get(0).get("fullName"));

        response = get("/organizations/jenkins/pipelines/folder1");
        Assert.assertEquals("folder1", response.get("name"));
        Assert.assertEquals("folder1", response.get("displayName"));
        Assert.assertEquals(2, response.get("numberOfFolders"));
        Assert.assertEquals(1, response.get("numberOfPipelines"));
        Assert.assertEquals("folder1", response.get("fullName"));

    }

    @Test
    public void getPipelinesTest() throws Exception {

        Project p2 = j.createFreeStyleProject("pipeline2");
        Project p1 = j.createFreeStyleProject("pipeline1");

        List<Map> responses = get("/search/?q=type:pipeline", List.class);
        Assert.assertEquals(2, responses.size());
        validatePipeline(p1, responses.get(0));
        validatePipeline(p2, responses.get(1));

        p1.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = (FreeStyleBuild) p1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);


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
    public void getPipelineRunWithTestResult() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline4");
        p.getBuildersList().add(new Shell("echo '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<testsuite xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://maven.apache.org/surefire/maven-surefire-plugin/xsd/surefire-test-report.xsd\" name=\"io.jenkins.blueocean.jsextensions.JenkinsJSExtensionsTest\" time=\"35.7\" tests=\"1\" errors=\"0\" skipped=\"0\" failures=\"0\">\n" +
            "  <properties>\n" +
            "  </properties>\n" +
            "  <testcase name=\"test\" classname=\"io.jenkins.blueocean.jsextensions.JenkinsJSExtensionsTest\" time=\"34.09\"/>\n" +
            "</testsuite>' > test-result.xml"));

        p.getPublishersList().add(new JUnitResultArchiver("*.xml"));
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        TestResultAction resultAction = b.getAction(TestResultAction.class);
        Assert.assertEquals("io.jenkins.blueocean.jsextensions.JenkinsJSExtensionsTest",resultAction.getResult().getSuites().iterator().next().getName());
        j.assertBuildStatusSuccess(b);
        Map resp = get("/organizations/jenkins/pipelines/pipeline4/runs/"+b.getId());

        //discover TestResultAction super classes
        get("/classes/hudson.tasks.junit.TestResultAction/");

        // get junit rest report
        get("/organizations/jenkins/pipelines/pipeline4/runs/"+b.getId()+"/testReport/result/");
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

        HttpResponse<String> response = get("/organizations/jenkins/pipelines/pipeline1/runs/"+b1.getId()+"/log?start=0", 200,HttpResponse.class);
        AcceptHeader acceptHeader = new AcceptHeader(response.getHeaders().getFirst("Content-Type"));
        Assert.assertNotNull(acceptHeader.select("text/plain"));

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
    public void findAllPipelineTest() throws IOException, ExecutionException, InterruptedException {
        MockFolder folder1 = j.createFolder("folder1");
        j.createFolder("afolder");
        Project p1 = folder1.createProject(FreeStyleProject.class, "test1");
        MockFolder folder2 = folder1.createProject(MockFolder.class, "folder2");
        folder1.createProject(MockFolder.class, "folder3");
        folder2.createProject(FreeStyleProject.class, "test2");

        FreeStyleBuild b1 = (FreeStyleBuild) p1.scheduleBuild2(0).get();


        List<Map> resp = get("/search?q=type:pipeline", List.class);

        Assert.assertEquals(6, resp.size());
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

    @Test
    public void testPipelineQueue() throws Exception {
        FreeStyleProject p1 = j.createFreeStyleProject("pipeline1");

        p1.setConcurrentBuild(true);
        p1.addProperty(new ParametersDefinitionProperty(new StringParameterDefinition("test","test")));
        p1.getBuildersList().add(new Shell("echo hello!\nsleep 300"));

        p1.scheduleBuild2(0).waitForStart();
        p1.scheduleBuild2(0).waitForStart();
        Jenkins.getInstance().getQueue().schedule(p1, 0, new ParametersAction(new StringParameterValue("test","test1")), new CauseAction(new Cause.UserIdCause()));
        Jenkins.getInstance().getQueue().schedule(p1, 0, new ParametersAction(new StringParameterValue("test","test2")), new CauseAction(new Cause.UserIdCause()));

        List queue = request().get("/organizations/jenkins/pipelines/pipeline1/queue").build(List.class);
        Assert.assertEquals(queue.size(),2);
        Assert.assertEquals(((Map) queue.get(0)).get("expectedBuildNumber"), 4);
        Assert.assertEquals(((Map) queue.get(1)).get("expectedBuildNumber"), 3);
        System.out.println(request().get("/organizations/jenkins/pipelines/pipeline1/queue").build(String.class));

    }

    @Test
    public void testNewPipelineQueueItem() throws Exception {
        FreeStyleProject p1 = j.createFreeStyleProject("pipeline1");
        FreeStyleProject p2 = j.createFreeStyleProject("pipeline2");
        FreeStyleProject p3 = j.createFreeStyleProject("pipeline3");
        p1.getBuildersList().add(new Shell("echo hello!\nsleep 300"));
        p2.getBuildersList().add(new Shell("echo hello!\nsleep 300"));
        p3.getBuildersList().add(new Shell("echo hello!\nsleep 300"));
        p1.scheduleBuild2(0).waitForStart();
        p2.scheduleBuild2(0).waitForStart();

        Map r = request().post("/organizations/jenkins/pipelines/pipeline3/runs/").build(Map.class);

        Assert.assertNotNull(p3.getQueueItem());
        Assert.assertEquals(Long.toString(p3.getQueueItem().getId()), r.get("id"));
    }

    @Test
    public void getPipelinesExtensionTest() throws Exception {

        Project p = j.createFreeStyleProject("pipeline1");

        Map<String,Object> response = get("/organizations/jenkins/pipelines/pipeline1");
        validatePipeline(p, response);

        Assert.assertEquals("hello world!", response.get("hello"));
    }

    @Extension(ordinal = 3)
    public static class PipelineFactoryTestImpl extends BluePipelineFactory {

        @Override
        public BluePipeline getPipeline(Item item, Reachable parent) {
            if(item instanceof Job){
                return new TestPipelineImpl((Job)item);
            }
            return null;
        }

        @Override
        public Resource resolve(Item context, Reachable parent, Item target) {
            return  null;
        }
    }

    @Capability({"io.jenkins.blueocean.rest.annotation.test.TestPipeline", "io.jenkins.blueocean.rest.annotation.test.TestPipelineExample"})
    public static class TestPipelineImpl extends PipelineImpl {

        public TestPipelineImpl(Job job) {
            super(job);
        }

        @Exported(name = "hello")
        public String getHello(){
            return "hello world!";
        }
    }

    @Test
    public void testCapabilityAnnotation(){
        Map resp = get("/classes/"+TestPipelineImpl.class.getName());
        List<String> classes = (List<String>) resp.get("classes");
        Assert.assertEquals("io.jenkins.blueocean.rest.annotation.test.TestPipeline", classes.get(0));
        Assert.assertEquals("io.jenkins.blueocean.rest.annotation.test.TestPipelineExample", classes.get(1));
    }


}
