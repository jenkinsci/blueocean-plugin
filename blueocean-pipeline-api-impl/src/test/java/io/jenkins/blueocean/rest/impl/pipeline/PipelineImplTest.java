package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import hudson.model.Result;
import hudson.model.Run;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jenkins.plugins.git.GitSampleRepoRule;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import static org.junit.Assert.assertTrue;

public class PipelineImplTest extends PipelineBaseTest {
    
    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Test
    @Issue("JENKINS-55497")
    public void testPipelineRunSummaryHasChangeSet() throws Exception {
        String jenkinsFile = Resources.toString(Resources.getResource(getClass(), "singleScm.jenkinsfile"), Charsets.UTF_8).replaceAll("%REPO%", sampleRepo.toString());

        WorkflowJob p = j.createProject(WorkflowJob.class, "project");
        p.setDefinition(new CpsFlowDefinition(jenkinsFile, true));
        p.save();

        sampleRepo.init();

        j.assertBuildStatus(Result.SUCCESS, p.scheduleBuild2(0));
        
        // create a commit to populate the changeSet for the second run
        sampleRepo.write("file1", "");
        sampleRepo.git("add", "file1");
        sampleRepo.git("commit", "--message=init");

        Run r = j.assertBuildStatus(Result.SUCCESS, p.scheduleBuild2(0));

        // confirm the changeSet retrieved from the latestRun details for the project is not empty
        Map<String, Object> runDetails = get("/organizations/jenkins/pipelines/" + p.getName() + "/runs/" + r.getId() + "/");
        List<Object> changeSet = (ArrayList) runDetails.get("changeSet");
        assertTrue(!changeSet.isEmpty());
    }
}