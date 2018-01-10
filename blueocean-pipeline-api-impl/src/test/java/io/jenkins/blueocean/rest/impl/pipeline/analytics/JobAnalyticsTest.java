package io.jenkins.blueocean.rest.impl.pipeline.analytics;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import hudson.matrix.MatrixProject;
import hudson.model.CauseAction;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import io.jenkins.blueocean.analytics.Analytics;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.service.embedded.analytics.AbstractAnalytics;
import io.jenkins.blueocean.service.embedded.analytics.JobAnalytics;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;

import java.net.URL;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JobAnalyticsTest extends PipelineBaseTest {

    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Rule
    public GitSampleRepoRule sampleRepo2 = new GitSampleRepoRule();

    @Before
    public void setup() throws Exception {
        sampleRepo.init();
        sampleRepo.git("checkout","master");
        sampleRepo.write("Jenkinsfile", Resources.toString(Resources.getResource(JobAnalyticsTest.class, "JobAnalyticsTest-scripted.jenkinsfile"), Charsets.UTF_8));
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--all", "--message=Jenkinsfile");

        sampleRepo2.init();
        sampleRepo2.git("checkout","master");
        sampleRepo2.write("Jenkinsfile", Resources.toString(Resources.getResource(JobAnalyticsTest.class, "JobAnalyticsTest-declarative.jenkinsfile"), Charsets.UTF_8));
        sampleRepo2.git("add", "Jenkinsfile");
        sampleRepo2.git("commit", "--all", "--message=Jenkinsfile");
    }

    @Test
    public void testJobAnalytics() throws Exception {
        // Freestyle jobs
        j.createFreeStyleProject("freestyle1").save();
        j.createFreeStyleProject("freestyle2").save();

        // Matrix job
        j.createProject(MatrixProject.class, "bob");

        // Create single scripted pipeline
        WorkflowJob scriptedSingle = createWorkflowJobWithJenkinsfile(getClass(),"JobAnalyticsTest-scripted.jenkinsfile");
        WorkflowRun scriptedSingleRun = scriptedSingle.scheduleBuild2(0, new CauseAction()).waitForStart();
        j.waitForCompletion(scriptedSingleRun);

        // Create single declarative pipeline
        WorkflowJob declarativeSingle = createWorkflowJobWithJenkinsfile(getClass(),"JobAnalyticsTest-declarative.jenkinsfile");
        WorkflowRun declarativeSingleRun = declarativeSingle.scheduleBuild2(0, new CauseAction()).waitForStart();
        j.waitForCompletion(declarativeSingleRun);

        // Create Scripted MultiBranch
        createMultiBranch(sampleRepo);

        // Create Declarative MultiBranch
        createMultiBranch(sampleRepo2);

        AnalyticsImpl analytics = (AnalyticsImpl)Analytics.get();
        Assert.assertNotNull(analytics);

        JobAnalytics jobAnalytics = new JobAnalytics();
        jobAnalytics.calculateAndSend();

        Assert.assertNotNull(analytics.lastReq);
        Assert.assertEquals("job_stats", analytics.lastReq.name);

        Map<String, Object> properties = analytics.lastReq.properties;
        Assert.assertEquals("singlePipelineDeclarative", 1, properties.get("singlePipelineDeclarative"));
        Assert.assertEquals("singlePipelineScripted",1, properties.get("singlePipelineScripted"));
        Assert.assertEquals("pipelineDeclarative",1, properties.get("pipelineDeclarative"));
        Assert.assertEquals("pipelineScripted",1, properties.get("pipelineScripted"));
        Assert.assertEquals("freestyle",2, properties.get("freestyle"));
        Assert.assertEquals("matrix",1, properties.get("matrix"));
        Assert.assertEquals("other",0, properties.get("other"));
    }

    private void createMultiBranch(GitSampleRepoRule rule) throws Exception {
        WorkflowMultiBranchProject mp = j.createProject(WorkflowMultiBranchProject.class, UUID.randomUUID().toString());
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, rule.toString(), "", "*", "", false),
            new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        mp.save();
        mp.scheduleBuild2(0).getFuture().get();
        mp.getIndexing().getLogText().writeLogTo(0, System.out);
        j.waitUntilNoActivity();
        WorkflowJob master = mp.getItemByBranchName("master");
        assertNotNull(master);
        WorkflowRun lastBuild = master.getLastBuild();
        assertNotNull(lastBuild);
        assertEquals(Result.SUCCESS, lastBuild.getResult());
    }

    @TestExtension
    public static class AnalyticsImpl extends AbstractAnalytics {

        TrackRequest lastReq;

        @Override
        protected void doTrack(String name, Map<String, Object> allProps) {
            lastReq = new TrackRequest(name, allProps);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
