package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.Label;
import hudson.model.Queue;
import io.jenkins.blueocean.rest.impl.pipeline.scm.GitSampleRepoRule;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Ivan Meredith
 */
public class AbstractRunImplTest extends PipelineBaseTest {
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

        Map r = request().get("/organizations/jenkins/pipelines/pipeline1/runs/3/").build(Map.class);
        assertEquals(r.get("commitId"), new PipelineRunImpl(b2,null).getCommitId());
    }

    // Disabled, see JENKINS-40084
    // @Test
    public void replayRunTestMB() throws Exception {
        j.createOnlineSlave(Label.get("remote"));

        sampleRepo.write("Jenkinsfile", "node('remote') {\n" +
            "    ws {\n" +
            "       checkout scm\n" +
            "       stage 'build'\n "+"node {echo 'Building'}\n"+
            "       stage 'test'\nnode { echo 'Testing'}\n"+
            "       stage 'deploy'\nnode { echo 'Deploying'}\n" +
            "       }\n" +
            "   }");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=init");

        WorkflowMultiBranchProject mp = j.jenkins.createProject(WorkflowMultiBranchProject.class, "p");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false),
            new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        for (SCMSource source : mp.getSCMSources()) {
            assertEquals(mp, source.getOwner());
        }


        mp.scheduleBuild2(0).getFuture().get();
        WorkflowJob job1 = mp.getItem("master");
        WorkflowRun b1 = job1.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(b1);
        j.assertBuildStatusSuccess(b1);

        sampleRepo.write("file1", "");
        sampleRepo.git("add", "file1");
        sampleRepo.git("commit", "--message=init");

        WorkflowRun b2 = job1.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b2);

        Assert.assertNotEquals(new PipelineRunImpl(b1, null).getCommitId(), new PipelineRunImpl(b2, null).getCommitId());

        Map replayBuild = request().post("/organizations/jenkins/pipelines/p/branches/master/runs/"+ b1.getNumber()+"/replay").build(Map.class);
        Queue.Item item = j.getInstance().getQueue().getItem(Long.parseLong((String)replayBuild.get("id")));

        WorkflowRun replayedRun = (WorkflowRun)item.getFuture().get();

        Map r = request().get("/organizations/jenkins/pipelines/p/branches/master/runs/"+replayedRun.getNumber()+"/").build(Map.class);
        assertEquals(new PipelineRunImpl(b1,null).getCommitId(), r.get("commitId"));
    }
}
