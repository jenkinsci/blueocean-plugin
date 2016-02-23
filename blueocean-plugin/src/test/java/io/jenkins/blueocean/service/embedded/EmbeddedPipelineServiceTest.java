package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableMap;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Shell;
import io.jenkins.blueocean.api.pipeline.FindPipelineRunsRequest;
import io.jenkins.blueocean.api.pipeline.FindPipelineRunsResponse;
import io.jenkins.blueocean.api.pipeline.FindPipelinesRequest;
import io.jenkins.blueocean.api.pipeline.FindPipelinesResponse;
import io.jenkins.blueocean.api.pipeline.GetPipelineRequest;
import io.jenkins.blueocean.api.pipeline.GetPipelineResponse;
import io.jenkins.blueocean.api.pipeline.GetPipelineRunRequest;
import io.jenkins.blueocean.api.pipeline.GetPipelineRunResponse;
import io.jenkins.blueocean.api.pipeline.PipelineService;
import io.jenkins.blueocean.security.Identity;
import io.jenkins.blueocean.commons.JsonConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

/**
 * @author Vivek Pandey
 */
public class EmbeddedPipelineServiceTest {
    private PipelineService pipelineService;

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void before(){
        List<PipelineService> PipelineService = j.jenkins.getExtensionList(PipelineService.class);
        Assert.assertTrue(PipelineService.size() == 1);
        this.pipelineService = PipelineService.get(0);
    }

    @Test
    public void getPipelineTest() throws IOException {
        j.createFreeStyleProject("pipeline1");
        GetPipelineResponse response = pipelineService.getPipeline(Identity.ANONYMOUS,
                new GetPipelineRequest("Jenkins", "pipeline1"));
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.pipeline);

        Assert.assertEquals("pipeline1", response.pipeline.name);
        Assert.assertEquals("Jenkins", response.pipeline.organization);
    }

    @Test
    public void findPipelinesTest() throws IOException {
        j.createFreeStyleProject("pipeline1");
        j.createFreeStyleProject("pipeline2");

        FindPipelinesResponse response = pipelineService.findPipelines(Identity.ANONYMOUS,
                new FindPipelinesRequest("Jenkins", "pipeline"));
        Assert.assertNotNull(response);
        Assert.assertEquals(2, response.pipelines.size());

        Assert.assertEquals("pipeline1", response.pipelines.get(0).name);
        Assert.assertEquals("Jenkins", response.pipelines.get(0).organization);
        Assert.assertEquals("pipeline2", response.pipelines.get(1).name);
        Assert.assertEquals("Jenkins", response.pipelines.get(1).organization);
    }


    @Test
    public void getPipelineRunTest() throws IOException, ExecutionException, InterruptedException {
        FreeStyleProject p = j.createFreeStyleProject("pipeline1");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        GetPipelineRunResponse response = pipelineService.getPipelineRun(Identity.ANONYMOUS,
                new GetPipelineRunRequest("Jenkins", "pipeline1"));
        System.out.println(JsonConverter.toJson(response));
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.run);
        Assert.assertEquals(b.getId(), response.run.id);
        Assert.assertEquals("Jenkins", response.run.organization);
        Assert.assertEquals("pipeline1", response.run.pipeline);
        Assert.assertEquals(b.getStartTimeInMillis(), response.run.startTime.getTime());
    }


    @Test
    public void findPipelineRunsForAPipelineTest() throws IOException, ExecutionException, InterruptedException {
        FreeStyleProject p1 = j.createFreeStyleProject("pipeline1");
        FreeStyleProject p2 = j.createFreeStyleProject("pipeline2");
        p1.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        p2.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        Stack<FreeStyleBuild> builds = new Stack<FreeStyleBuild>();
        builds.push(p1.scheduleBuild2(0).get());
        builds.push(p1.scheduleBuild2(0).get());

        FindPipelineRunsResponse response = pipelineService.findPipelineRuns(Identity.ANONYMOUS,
                new FindPipelineRunsRequest("Jenkins", "pipeline1", false, Collections.EMPTY_LIST, null, null));

        System.out.println("Response: "+JsonConverter.toJson(response));
        Assert.assertNotNull(response);
        Assert.assertEquals(builds.size(), response.runs.size());
        for(int i = 0;i < response.runs.size(); i++){
            FreeStyleBuild b = builds.pop();
            Assert.assertEquals(b.getId(), response.runs.get(i).id);
            Assert.assertEquals("Jenkins", response.runs.get(i).organization);
            Assert.assertEquals("pipeline1", response.runs.get(i).pipeline);
            Assert.assertEquals(b.getStartTimeInMillis(), response.runs.get(i).startTime.getTime());
        }
    }

    @Test
    public void findPipelineRunsForAllPipelineTest() throws IOException, ExecutionException, InterruptedException {
        FreeStyleProject p1 = j.createFreeStyleProject("pipeline1");
        FreeStyleProject p2 = j.createFreeStyleProject("pipeline2");
        p1.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        p2.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        Stack<FreeStyleBuild> p1builds = new Stack<FreeStyleBuild>();
        p1builds.push(p1.scheduleBuild2(0).get());
        p1builds.push(p1.scheduleBuild2(0).get());

        Stack<FreeStyleBuild> p2builds = new Stack<FreeStyleBuild>();
        p2builds.push(p2.scheduleBuild2(0).get());
        p2builds.push(p2.scheduleBuild2(0).get());

        Map<String, Stack<FreeStyleBuild>> buildMap = ImmutableMap.of(p1.getName(), p1builds, p2.getName(), p2builds);
        FindPipelineRunsResponse response = pipelineService.findPipelineRuns(Identity.ANONYMOUS,
                new FindPipelineRunsRequest("Jenkins", null, false, Collections.EMPTY_LIST, null, null));

        System.out.println("Response: "+JsonConverter.toJson(response));

        Assert.assertNotNull(response);
        Assert.assertEquals(p1builds.size() + p2builds.size(), response.runs.size());
        for(int i = 0;i < response.runs.size(); i++){
            FreeStyleBuild b = buildMap.get(response.runs.get(i).pipeline).pop();
            Assert.assertEquals(b.getId(), response.runs.get(i).id);
            Assert.assertEquals("Jenkins", response.runs.get(i).organization);
            Assert.assertEquals(b.getProject().getName(), response.runs.get(i).pipeline);
            Assert.assertEquals(b.getStartTimeInMillis(), response.runs.get(i).startTime.getTime());
        }
    }

    public void getPipelineRunForWorkflow(){
//        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, "Noddy Job");
//
//        String script = "node {" +
//                "  stage ('Stage 1');" +
//                "  echo ('hi');" +
//                "  checkpoint ('Before Stage 2');" +
//                "  stage ('Stage 2');" +
//                "  echo ('hi');" +
//                "  checkpoint ('Before Stage 3');" +
//                "  stage ('Stage 3');" +
//                "  echo ('hi');" +
//                "  sh ('break-the-build');" +  // will intentionally fail on linux as well as windows
//                "}";

    }

}
