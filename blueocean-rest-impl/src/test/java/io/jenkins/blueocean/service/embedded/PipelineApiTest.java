package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import hudson.model.Run;
import hudson.model.StringParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.security.HudsonPrivateSecurityRealm;
import hudson.security.LegacyAuthorizationStrategy;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Maven;
import hudson.tasks.Shell;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.junit.TestResultAction;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueArtifact;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineImpl;
import io.jenkins.blueocean.service.embedded.rest.ArtifactContainerImpl;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;
import io.jenkins.blueocean.service.embedded.rest.QueueUtil;
import jenkins.model.Jenkins;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.ExtractResourceSCM;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.ToolInstallations;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

/**
 * @author Vivek Pandey
 */
public class PipelineApiTest extends BaseTest {

    @Test
    public void getFolderPipelineTest() throws IOException {
        MockFolder folder = j.createFolder("folder1");
        Project p = folder.createProject(FreeStyleProject.class, "test1");

        Map response = get("/organizations/jenkins/pipelines/folder1/pipelines/test1");
        validatePipeline(p, response);
    }

    @Test
    public void linkStartLimitTest() throws IOException, UnirestException {
        MockFolder folder = j.createFolder("folder1");
        Project p = folder.createProject(FreeStyleProject.class, "test1");

        HttpResponse<String> response = Unirest.get(getBaseUrl("/organizations/jenkins/pipelines/folder1/pipelines/"))
                .header("Accept-Encoding","")
                .header("Authorization", "Bearer "+jwtToken)
                .asString();

        String link = response.getHeaders().get("Link").get(0);

        assertEquals("</jenkins/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/?start=100&limit=100>; rel=\"next\"", link);

        response = Unirest.get(getBaseUrl("/search/?q=type:pipeline;excludedFromFlattening:jenkins.branch.MultiBranchProject,hudson.matrix.MatrixProject&filter1=no-folders&start=0&limit=26"))
                .header("Accept-Encoding","")
                .header("Authorization", "Bearer "+jwtToken)
                .asString();

        link = response.getHeaders().get("Link").get(0);

        assertEquals("</jenkins/blue/rest/search/?q=type:pipeline;excludedFromFlattening:jenkins.branch.MultiBranchProject,hudson.matrix.MatrixProject&filter1=no-folders&start=26&limit=26>; rel=\"next\"", link);


        response = Unirest.get(getBaseUrl("/organizations/jenkins/pipelines/folder1/pipelines/?start=10&limit=10&foo=bar"))
                .header("Accept-Encoding","")
                .header("Authorization", "Bearer "+jwtToken)
                .asString();

        link = response.getHeaders().get("Link").get(0);

        assertEquals("</jenkins/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/?foo=bar&start=20&limit=10>; rel=\"next\"", link);
    }

    @Test
    public void getNestedFolderPipelineTest() throws IOException {
        MockFolder folder1 = j.createFolder("folder1");
        Project p1 = folder1.createProject(FreeStyleProject.class, "test1");
        MockFolder folder2 = folder1.createProject(MockFolder.class, "folder2");
        folder2.setDisplayName("My folder2");
        MockFolder folder3 = folder1.createProject(MockFolder.class, "folder3");
        Project p2 = folder2.createProject(FreeStyleProject.class, "test2");

        List<Map> topFolders = get("/organizations/jenkins/pipelines/", List.class);

        assertEquals(1, topFolders.size());

        Map response = get("/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/test2");
        validatePipeline(p2, response);

        List<Map> pipelines = get("/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/", List.class);
        assertEquals(1, pipelines.size());
        validatePipeline(p2, pipelines.get(0));

        pipelines = get("/organizations/jenkins/pipelines/folder1/pipelines/", List.class);
        assertEquals(3, pipelines.size());
        assertEquals("folder2", pipelines.get(0).get("name"));
        assertEquals("folder1/folder2", pipelines.get(0).get("fullName"));
        assertEquals("folder1/My%20folder2", pipelines.get(0).get("fullDisplayName"));

        response = get("/organizations/jenkins/pipelines/folder1");
        assertEquals("folder1", response.get("name"));
        assertEquals("folder1", response.get("displayName"));
        assertEquals(2, response.get("numberOfFolders"));
        assertEquals(1, response.get("numberOfPipelines"));
        assertEquals("folder1", response.get("fullName"));

        String clazz = (String) response.get("_class");

        response = get("/classes/"+clazz+"/");
        assertNotNull(response);

        List<String> classes = (List<String>) response.get("classes");
        assertTrue(!classes.contains("hudson.model.Job")
            && classes.contains("io.jenkins.blueocean.rest.model.BluePipeline")
            && classes.contains("io.jenkins.blueocean.rest.model.BluePipelineFolder")
            && classes.contains("com.cloudbees.hudson.plugins.folder.AbstractFolder"));
    }

    @Test
    public void testUnknownClassCapabilities(){
        Map response = get("/classes/blah12345/");
        assertNotNull(response);
        assertEquals(0, ((List)response.get("classes")).size());

        response = post("/classes/", ImmutableMap.of("q", ImmutableList.of("blah12345",TestPipelineImpl.class.getName())));
        assertNotNull(response);
        Map cap = (Map) response.get("map");
        assertEquals(2, cap.size());
        Map d  = (Map) cap.get("blah12345");
        assertEquals(0, ((List)d.get("classes")).size());
    }

    @Test
    public void getPipelinesTest() throws Exception {

        Project p2 = j.createFreeStyleProject("pipeline2");
        Project p1 = j.createFreeStyleProject("pipeline1");

        List<Map> responses = get("/search/?q=type:pipeline", List.class);
        assertEquals(2, responses.size());
        validatePipeline(p1, responses.get(0));
        validatePipeline(p2, responses.get(1));

        p1.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = (FreeStyleBuild) p1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);


    }

    @Test
    public void getPipelinesDefaultPaginationTest() throws Exception {

        for(int i=0; i < 110; i++){
            j.createFreeStyleProject("pipeline"+i);
        }

        List<Map> responses = get("/search/?q=type:pipeline", List.class);
        assertEquals(100, responses.size());

        responses = get("/search/?q=type:pipeline&limit=110", List.class);
        assertEquals(110, responses.size());


        responses = get("/search/?q=type:pipeline&limit=50", List.class);
        assertEquals(50, responses.size());

        responses = get("/organizations/jenkins/pipelines/", List.class);
        assertEquals(100, responses.size());

        responses = get("/organizations/jenkins/pipelines/?limit=40", List.class);
        assertEquals(40, responses.size());
    }


    @Test
    public void getPipelineTest() throws IOException {
        Project p = j.createFreeStyleProject("pipeline1");

        Map<String,Object> response = get("/organizations/jenkins/pipelines/pipeline1");
        validatePipeline(p, response);

        String clazz = (String) response.get("_class");

        response = get("/classes/"+clazz+"/");
        assertNotNull(response);

        List<String> classes = (List<String>) response.get("classes");
        assertTrue(classes.contains("hudson.model.Job")
            && !classes.contains("org.jenkinsci.plugins.workflow.job.WorkflowJob")
            && !classes.contains("io.jenkins.blueocean.rest.model.BlueBranch"));
    }

    /** TODO: latest stapler change broke delete, disabled for now */
//    @Test
    public void deletePipelineTest() throws IOException {
        Project p = j.createFreeStyleProject("pipeline1");

        delete("/organizations/jenkins/pipelines/pipeline1/");

        assertNull(j.jenkins.getItem(p.getName()));
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

        assertEquals(projects.length, resp.size());

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

        assertEquals(projects.length, resp.size());

        for(int i=0; i<projects.length; i++){
            Map p = resp.get(i);
            validatePipeline(projects[i], p);
        }
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
        assertEquals("io.jenkins.blueocean.jsextensions.JenkinsJSExtensionsTest",resultAction.getResult().getSuites().iterator().next().getName());
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
        String log = get("/organizations/jenkins/pipelines/pipeline4/runs/"+b.getId()+"/log", String.class);
        System.out.println(log);
        assertNotNull(log);
    }

    @Test
    public void getPipelineRunLatestTest() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline5");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);

        List<Map> resp = get("/search?q=type:run;organization:jenkins;pipeline:pipeline5;latestOnly:true", List.class);
        Run[] run = {b};

        assertEquals(run.length, resp.size());

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
        assertEquals(1, resp.size());

        Map lr = resp.get(0);
        validateRun(b, lr);
    }

    @Test
    public void shouldFailToGetRunForInvalidRunId1() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline6");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);
        get("/organizations/jenkins/pipelines/pipeline6/runs/xyz", 404, Map.class);
    }

    @Test
    public void shouldFailToGetRunForInvalidRunId2() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline6");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        get("/organizations/jenkins/pipelines/pipeline6/runs/xyz", 404, Map.class);
    }

    @Test
    public void getPipelineRunsStopTest() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("p1");
        p.getBuildersList().add(new Shell("sleep 600000"));
        FreeStyleBuild b = p.scheduleBuild2(0).waitForStart();

        //wait till its running
        do{
            Thread.sleep(10); //sleep for 10ms
        }while(b.hasntStartedYet());

        Map resp = put("/organizations/jenkins/pipelines/p1/runs/"+b.getId()+"/stop/?blocking=true", Map.class);

        // we can't actually guarantee that jenkins will stop it
        assertTrue(resp.get("result").equals("ABORTED") || resp.get("result").equals("UNKNOWN"));
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

        assertEquals(builds.size(), resp.size());
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

        assertEquals(6, resp.size());
    }

    @Test
    public void findAllPipelineByGlob() throws IOException, ExecutionException, InterruptedException {
        MockFolder folder1 = j.createFolder("folder1");
        j.createFolder("afolder");
        Project p1 = folder1.createProject(FreeStyleProject.class, "test1");
        MockFolder folder2 = folder1.createProject(MockFolder.class, "folder2");
        folder1.createProject(MockFolder.class, "folder3");
        folder2.createProject(FreeStyleProject.class, "test2");
        folder2.createProject(FreeStyleProject.class, "coolPipeline");
        List<Map> resp = get("/search?q=type:pipeline;pipeline:*TEST*", List.class);
        assertEquals(2, resp.size());

        resp = get("/search?q=type:pipeline;pipeline:*cool*", List.class);
        assertEquals(1, resp.size());

        resp = get("/search?q=type:pipeline;pipeline:*nothing*", List.class);
        assertEquals(0, resp.size());

        resp = get("/search?q=type:pipeline;pipeline:*f*", List.class);
        assertEquals(7, resp.size());
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

        assertEquals(4, resp.size());
        for(int i=0; i< 4; i++){
            Map p = resp.get(i);
            String pipeline = (String) p.get("pipeline");
            assertNotNull(pipeline);
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


        List artifacts = get("/organizations/jenkins/pipelines/pipeline1/runs/"+b.getId()+"/artifacts", List.class);

        assertEquals(1, artifacts.size());
        assertEquals("fizz", ((Map) artifacts.get(0)).get("name"));

        BlueArtifact blueArtifact = new ArtifactContainerImpl(b, new Reachable() {
            @Override
            public Link getLink() {
                return new Link("/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/1/artifacts/");
            }
        }).get((String) ((Map) artifacts.get(0)).get("path"));
        assertNotNull(blueArtifact);
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
        Assert.assertEquals(2, queue.size());
        Assert.assertEquals(4, ((Map) queue.get(0)).get("expectedBuildNumber"));
        Assert.assertEquals(3, ((Map) queue.get(1)).get("expectedBuildNumber"));
        Assert.assertEquals("Waiting for next available executor", ((Map) queue.get(0)).get("causeOfBlockage"));
        Assert.assertEquals("Waiting for next available executor", ((Map) queue.get(1)).get("causeOfBlockage"));

        Run r = QueueUtil.getRun(p1, Long.parseLong((String)((Map)queue.get(0)).get("id")));
        assertNull(r); //its not moved out of queue yet
    }

    @Test
    public void testNewPipelineQueueItem() throws Exception {
        // We always want the first two jobs to be executing

        j.jenkins.setNumExecutors(2);

        FreeStyleProject p1 = j.createFreeStyleProject("pipeline1");
        FreeStyleProject p2 = j.createFreeStyleProject("pipeline2");
        FreeStyleProject p3 = j.createFreeStyleProject("pipeline3");
        p1.getBuildersList().add(new Shell("echo hello!\nsleep 100000"));
        p2.getBuildersList().add(new Shell("echo hello!\nsleep 100000"));
        p3.getBuildersList().add(new Shell("echo hello!\nsleep 100000"));

        // Kick off the first two jobs
        p1.scheduleBuild2(0).waitForStart();
        p2.scheduleBuild2(0).waitForStart();

        // Run the third pipeline
        Map r = request().post("/organizations/jenkins/pipelines/pipeline3/runs/").build(Map.class);

        // Ensure it is still in the queue
        assertNotNull(p3.getQueueItem());
        String id = Long.toString(p3.getQueueItem().getId());

        // Queue id matches the one we get back from the rest API
        assertEquals(id, r.get("queueId"));

        // Remove from queue
        delete("/organizations/jenkins/pipelines/pipeline3/queue/"+id+"/");

        // Make sure it is no longer in the queue
        // but handle the async nature of the queue otherwise we will intermittently fail this test on slow machines
        List<Map> build = request().get("/organizations/jenkins/pipelines/pipeline3/queue/").build(List.class);
        long end = TimeUnit.SECONDS.toMillis(10) + System.currentTimeMillis();
        while (!build.isEmpty() || System.currentTimeMillis() < end) {
            build = request().get("/organizations/jenkins/pipelines/pipeline3/queue/").build(List.class);
        }

        assertEquals(0, build.size());
    }

    @Test
    public void getPipelinesExtensionTest() throws Exception {

        Project p = j.createProject(TestProject.class,"pipeline1");

        Map<String,Object> response = get("/organizations/jenkins/pipelines/pipeline1");
        validatePipeline(p, response);

        assertEquals("hello world!", response.get("hello"));
    }

    @Extension(ordinal = 3)
    public static class PipelineFactoryTestImpl extends BluePipelineFactory {

        @Override
        public BluePipeline getPipeline(Item item, Reachable parent) {
            if(item instanceof TestProject){
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
    public static class TestPipelineImpl extends AbstractPipelineImpl {

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
        assertEquals("io.jenkins.blueocean.rest.annotation.test.TestPipeline", classes.get(0));
        assertEquals("io.jenkins.blueocean.rest.annotation.test.TestPipelineExample", classes.get(1));
    }

    @Test
    public void testClassesQueryWithPost(){
        // get classes for given class
        Map resp = get("/classes/"+TestPipelineImpl.class.getName());
        assertNotNull(resp);
        List<String> classes = (List<String>) resp.get("classes");
        assertTrue(classes.contains("io.jenkins.blueocean.rest.model.BluePipeline"));


        // should return empty map
        resp = post("/classes/", Collections.EMPTY_MAP);
        assertNotNull(resp);
        Map m = (Map) resp.get("map");
        assertTrue(m.isEmpty());

        resp = post("/classes/", ImmutableMap.of("q", ImmutableList.of("io.jenkins.blueocean.service.embedded.rest.AbstractPipelineImpl",TestPipelineImpl.class.getName())));
        assertNotNull(resp);
        m = (Map) resp.get("map");
        assertNotNull(m);
        assertEquals(2, m.size());


        Map v = (Map) m.get("io.jenkins.blueocean.service.embedded.rest.AbstractPipelineImpl");
        assertNotNull(v);

        classes = (List<String>) v.get("classes");
        assertTrue(classes.contains("io.jenkins.blueocean.rest.model.BluePipeline"));

        v = (Map) m.get(TestPipelineImpl.class.getName());
        assertNotNull(v);

        classes = (List<String>) v.get("classes");
        assertTrue(classes.contains("io.jenkins.blueocean.rest.model.BluePipeline"));
    }


    @Test
    public void PipelineUnsecurePermissionTest() throws IOException {
        MockFolder folder = j.createFolder("folder1");

        Project p = folder.createProject(FreeStyleProject.class, "test1");

        Map response = get("/organizations/jenkins/pipelines/folder1/pipelines/test1");
        validatePipeline(p, response);

        Map<String,Boolean> permissions = (Map<String, Boolean>) response.get("permissions");
        assertTrue(permissions.get("create"));
        assertTrue(permissions.get("start"));
        assertTrue(permissions.get("stop"));
        assertTrue(permissions.get("read"));
    }

    @Test
    public void PipelineSecureWithAnonymousUserPermissionTest() throws IOException {
        j.jenkins.setSecurityRealm(new HudsonPrivateSecurityRealm(false));
        j.jenkins.setAuthorizationStrategy(new LegacyAuthorizationStrategy());

        MockFolder folder = j.createFolder("folder1");

        Project p = folder.createProject(FreeStyleProject.class, "test1");

        Map response = get("/organizations/jenkins/pipelines/folder1/pipelines/test1");
        validatePipeline(p, response);

        Map<String,Boolean> permissions = (Map<String, Boolean>) response.get("permissions");
        Assert.assertFalse(permissions.get("create"));
        Assert.assertFalse(permissions.get("start"));
        Assert.assertFalse(permissions.get("stop"));
        assertTrue(permissions.get("read"));

        response = get("/organizations/jenkins/pipelines/folder1/");

        permissions = (Map<String, Boolean>) response.get("permissions");
        Assert.assertFalse(permissions.get("create"));
        Assert.assertFalse(permissions.get("start"));
        Assert.assertFalse(permissions.get("stop"));
        assertTrue(permissions.get("read"));
    }

    @Test
    public void PipelineSecureWithLoggedInUserPermissionTest() throws IOException, UnirestException {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());

        hudson.model.User user = j.jenkins.getUser("alice");
        user.setFullName("Alice Cooper");


        MockFolder folder = j.createFolder("folder1");

        Project p = folder.createProject(FreeStyleProject.class, "test1");
        String token = getJwtToken(j.jenkins, "alice", "alice");
        assertNotNull(token);
        Map response = new RequestBuilder(baseUrl)
            .get("/organizations/jenkins/pipelines/folder1/pipelines/test1")
            .jwtToken(token)
            .build(Map.class);

        validatePipeline(p, response);

        Map<String,Boolean> permissions = (Map<String, Boolean>) response.get("permissions");
        assertTrue(permissions.get("create"));
        assertTrue(permissions.get("start"));
        assertTrue(permissions.get("stop"));
        assertTrue(permissions.get("read"));
    }

    @Test
    public void parameterizedFreestyleTest() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pp");
        p.addProperty(new ParametersDefinitionProperty(new StringParameterDefinition("version", "1.0", "version number")));
        p.getBuildersList().add(new Shell("echo hello!"));

        Map resp = get("/organizations/jenkins/pipelines/pp/");

        List<Map<String,Object>> parameters = (List<Map<String, Object>>) resp.get("parameters");
        assertEquals(1, parameters.size());
        assertEquals("version", parameters.get(0).get("name"));
        assertEquals("StringParameterDefinition", parameters.get(0).get("type"));
        assertEquals("version number", parameters.get(0).get("description"));
        assertEquals("1.0", ((Map)parameters.get(0).get("defaultParameterValue")).get("value"));
        validatePipeline(p, resp);

        resp = post("/organizations/jenkins/pipelines/pp/runs/",
                ImmutableMap.of("parameters",
                        ImmutableList.of(ImmutableMap.of("name", "version", "value", "2.0"))
                ), 200);
        assertEquals("pp", resp.get("pipeline"));
        Thread.sleep(1000);
        resp = get("/organizations/jenkins/pipelines/pp/runs/1/");
        assertEquals("SUCCESS", resp.get("result"));
        assertEquals("FINISHED", resp.get("state"));
    }

    public static class TestStringParameterDefinition extends StringParameterDefinition{

        public TestStringParameterDefinition(String name, String defaultValue, String description) {
            super(name, defaultValue, description);
        }

        @Override
        public StringParameterValue getDefaultParameterValue() {
            return null;
        }
    }

    @Test
    public void parameterizedFreestyleTestWithoutDefaultParam() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pp");
        p.addProperty(new ParametersDefinitionProperty(new TestStringParameterDefinition("version", null, "version number")));
        p.getBuildersList().add(new Shell("echo hello!"));

        Map resp = get("/organizations/jenkins/pipelines/pp/");

        List<Map<String,Object>> parameters = (List<Map<String, Object>>) resp.get("parameters");
        assertEquals(1, parameters.size());
        assertEquals("version", parameters.get(0).get("name"));
        assertEquals("TestStringParameterDefinition", parameters.get(0).get("type"));
        assertEquals("version number", parameters.get(0).get("description"));
        assertNull(parameters.get(0).get("defaultParameterValue"));
        validatePipeline(p, resp);

        resp = post("/organizations/jenkins/pipelines/pp/runs/",
                ImmutableMap.of("parameters",
                        ImmutableList.of()
                ), 400);
    }

    @Test public void mavenModulesNoteListed() throws Exception {
        ToolInstallations.configureDefaultMaven("apache-maven-2.2.1", Maven.MavenInstallation.MAVEN_21);
        MavenModuleSet m = j.jenkins.createProject(MavenModuleSet.class, "p");
        m.setScm(new ExtractResourceSCM(getClass().getResource("maven-multimod.zip")));
        assertFalse("MavenModuleSet.isNonRecursive() should be false", m.isNonRecursive());
        j.buildAndAssertSuccess(m);

        List responses = get("/organizations/jenkins/pipelines/", List.class);
        assertEquals(1, responses.size());
        assertEquals("p", ((Map) responses.get(0)).get("name"));

    }

    @Test
    public void actionsTest() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline1");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);

        Map resp = get("/organizations/jenkins/pipelines/pipeline1/", Map.class);
        List<Map> actions = (List<Map>) resp.get("actions");
        Assert.assertTrue(actions.isEmpty());
        actions = (List<Map>) ((Map)resp.get("latestRun")).get("actions");

        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/", Map.class);
        actions = (List<Map>) resp.get("actions");
        Assert.assertTrue(actions.isEmpty());

        resp = get("/organizations/jenkins/pipelines/pipeline1/runs/1/?tree=*[*]", Map.class);
        actions = (List<Map>) resp.get("actions");
        Assert.assertEquals(2, actions.size());
    }

    /**
     * Tests the API when the organization root is a folder instead of the root jenkins. That organization should only
     * see the elements under that folder.
     */
    @Test
    @Issue({ "JENKINS-44176", "JENKINS-44270" })
    public void testOrganizationFolder() throws IOException, ExecutionException, InterruptedException {

        FreeStyleProject jobOutSideOrg = j.createFreeStyleProject("pipelineOutsideOrgName");
        jobOutSideOrg.setDisplayName("pipelineOutsideOrg Display Name");

        MockFolder orgFolder = j.createFolder("TestOrgFolderName");
        orgFolder.setDisplayName("TestOrgFolderName Display Name");

        MockFolder folderOnOrg = orgFolder.createProject(MockFolder.class, "folderOnOrgName");
        folderOnOrg.setDisplayName("folderOnOrg Display Name");

        FreeStyleProject jobOnRootOrg = orgFolder.createProject(FreeStyleProject.class, "jobOnRootOrgName");
        jobOnRootOrg.setDisplayName("jobOnRootOrg Display Name");

        FreeStyleProject jobOnFolder = folderOnOrg.createProject(FreeStyleProject.class, "jobOnFolderName");
        jobOnFolder.setDisplayName("jobOnFolder Display Name");

        List<Map> pipelines = get("/search/?q=type:pipeline;organization:TestOrg;excludedFromFlattening:jenkins.branch.MultiBranchProject,hudson.matrix.MatrixProject", List.class);

        //Only what's inside the org folder should be returned
        Assert.assertEquals(3, pipelines.size());

        //The full name should not contain the organization folder name
        Map links;
        for (Map map : pipelines) {
            Assert.assertEquals("TestOrg", map.get("organization"));
            if (map.get("name").equals("folderOnOrgName")) {
                map.get("fullDisplayName").equals("folderOnOrg%20Display%20Name");
                map.get("fullName").equals("folderOnOrgName");
                checkLinks((Map) map.get("_links"), "/blue/rest/organizations/TestOrg/pipelines/folderOnOrgName/");
            } else if (map.get("name").equals("jobOnRootOrgName")) {
                map.get("fullDisplayName").equals("jobOnRootOrg%20Display%20Name");
                map.get("fullName").equals("jobOnRootOrgName");
                checkLinks((Map) map.get("_links"), "/blue/rest/organizations/TestOrg/pipelines/jobOnRootOrgName/");
            } else if (map.get("name").equals("jobOnFolderName")) {
                map.get("fullDisplayName").equals("folderOnOrg%20Display%20Name/jobOnFolder%20Display%20Name");
                map.get("fullName").equals("folderOnOrgName/jobOnFolderName");
                checkLinks((Map) map.get("_links"), "/blue/rest/organizations/TestOrg/pipelines/folderOnOrgName/pipelines/jobOnFolderName/");
            } else {
                Assert.fail("Item " + map.get("name") + " shouldn't be present");
            }
        }
    }

    private void checkLinks(Map links, String startWith) {
        for (Object link : links.values()) {
            Map linkMap = (Map) link;
            String href = ((String) linkMap.get("href"));
            Assert.assertTrue("Link should start with " + startWith + " but was " + href, href.startsWith(startWith));
        }
    }

    @TestExtension(value = "testOrganizationFolder")
    public static class TestOrganizationFactoryImpl extends OrganizationFactoryImpl {
        private OrganizationImpl instance = new OrganizationImpl("TestOrg", Jenkins.getInstance().getItem("/TestOrgFolderName", Jenkins.getInstance(), MockFolder.class));

        @Override
        public OrganizationImpl get(String name) {
            if (instance != null) {
                if (instance.getName().equals(name)) {
                    System.out.println("" + name + " Intance returned " + instance);
                    return instance;
                }
            }
            System.out.println("" + name + " no instance found");
            return null;
        }

        @Override
        public Collection<BlueOrganization> list() {
            return Collections.singleton((BlueOrganization) instance);
        }

        @Override
        public OrganizationImpl of(ItemGroup group) {
            if (group == instance.getGroup()) {
                return instance;
            }
            return null;
        }
    }
}
