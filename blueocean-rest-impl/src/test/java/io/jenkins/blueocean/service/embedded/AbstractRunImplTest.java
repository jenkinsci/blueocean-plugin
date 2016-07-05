package io.jenkins.blueocean.service.embedded;

import hudson.model.Label;
import io.jenkins.blueocean.service.embedded.rest.PipelineImpl;
import io.jenkins.blueocean.service.embedded.rest.PipelineRunImpl;
import io.jenkins.blueocean.service.embedded.scm.GitSampleRepoRule;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.replay.ReplayAction;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

/**
 * @author Ivan Meredith
 */
public class AbstractRunImplTest extends BaseTest {
    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Before
    public void setup() throws Exception{
        super.setup();
        sampleRepo.init();
    }
    //Disabled, see JENKINS-36453
    //@Test
    public void replayRunTest() throws Exception {
        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        j.createOnlineSlave(Label.get("remote"));
        job1.setDefinition(new CpsFlowDefinition(
            "node('remote') {\n" +
                "    ws {\n" +
                "        git($/" + sampleRepo + "/$)\n" +
                "    }\n" +
                "}"));


        WorkflowRun b1 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b1);

        sampleRepo.write("file1", "");
        sampleRepo.git("add", "file1");
        sampleRepo.git("commit", "--message=init");

        WorkflowRun b2 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b2);

        Assert.assertNotEquals(new PipelineRunImpl(b1, null).getCommitId(), new PipelineRunImpl(b2, null).getCommitId());

        request().post("/organizations/jenkins/pipelines/pipeline1/runs/1/replay").build(String.class);

        j.waitForCompletion(job1.getLastBuild());

        request().get("/organizations/jenkins/pipelines/pipeline1/runs/1/").build(Map.class);
        request().get("/organizations/jenkins/pipelines/pipeline1/runs/2/").build(Map.class);
        Map r = request().get("/organizations/jenkins/pipelines/pipeline1/runs/3/").build(Map.class);
        Assert.assertEquals(r.get("commitId"), new PipelineRunImpl(b2,null).getCommitId());



    }
}
