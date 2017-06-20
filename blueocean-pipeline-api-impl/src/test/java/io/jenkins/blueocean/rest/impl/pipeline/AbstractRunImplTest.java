package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Shell;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.net.URL;
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

    @Test
    public void testArtifactZipFileLink() throws Exception {
        String JOB_NAME = "artifactTest";
        FreeStyleProject p = j.createFreeStyleProject(JOB_NAME);
        p.getBuildersList().add(new Shell("touch {{a..z},{A..Z},{0..99}}.txt"));
        p.getPublishersList().add(new ArtifactArchiver("*"));
        Run r = p.scheduleBuild2(0).waitForStart();

        r = j.waitForCompletion(r);

        Map m = request().get("/organizations/jenkins/pipelines/"+JOB_NAME+"/runs/"+r.getId()+"/").build(Map.class);

        Assert.assertEquals(m.get("artifactsZipFile"), "/job/artifactTest/1/artifact/*zip*/archive.zip");
    }

    @Test(timeout = 20000)
    @Issue("JENKINS-44736")
    public void earlyUnstableStatusShouldReportPunStateAsRunningAndResultAsUnknown() throws Exception {
        WorkflowJob p = j.createProject(WorkflowJob.class, "project");

        URL resource = Resources.getResource(getClass(), "earlyUnstableStatusShouldReportPunStateAsRunningAndResultAsUnknown.jenkinsfile");
        String jenkinsFile = Resources.toString(resource, Charsets.UTF_8);
        p.setDefinition(new CpsFlowDefinition(jenkinsFile, true));
        p.save();

        Run r = p.scheduleBuild2(0).waitForStart();

        String url = "/organizations/jenkins/pipelines/project/runs/" + r.getId() + "/";
        Map m = request().get(url).build(Map.class);

        // While the run has not finished keep checking that the result is unknown
        while (!"FINISHED".equals(m.get("state").toString())) {
            Assert.assertEquals("RUNNING", m.get("state"));
            Assert.assertEquals("UNKNOWN", m.get("result"));
            Thread.sleep(1000);
            m = request().get(url).build(Map.class);
        }

        // Ensure that the run has finished and was marked as unstable when completed
        Assert.assertEquals("FINISHED", m.get("state"));
        Assert.assertEquals("UNSTABLE", m.get("result"));
    }
}
