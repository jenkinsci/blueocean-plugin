package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import jenkins.plugins.git.GitSampleRepoRule;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.replay.ReplayAction;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class PipelineRunImplTest extends PipelineBaseTest {

    @Rule
    public GitSampleRepoRule sampleRepo1 = new GitSampleRepoRule();
    @Rule
    public GitSampleRepoRule sampleRepo2 = new GitSampleRepoRule();

    @Test
    @Issue("JENKINS-53019")
    public void testMultipleRepoChangeSet() throws Exception {
        String jenkinsFile = Resources.toString(Resources.getResource(getClass(), "mulitpleScms.jenkinsfile"), Charsets.UTF_8).replaceAll("%REPO1%", sampleRepo1.toString()).replaceAll("%REPO2%", sampleRepo2.toString());

        WorkflowJob p = j.createProject(WorkflowJob.class, "project");
        p.setDefinition(new CpsFlowDefinition(jenkinsFile, true));
        p.save();

        sampleRepo1.init();
        sampleRepo2.init();

        j.assertBuildStatus(Result.SUCCESS, p.scheduleBuild2(0));

        updateREADME(sampleRepo1);
        updateREADME(sampleRepo2);
        TimeUnit.SECONDS.sleep(1);
        updateREADME(sampleRepo2);
        TimeUnit.SECONDS.sleep(1);

        Run r = j.assertBuildStatus(Result.SUCCESS, p.scheduleBuild2(0));

        Map<String, Object> runDetails = get("/organizations/jenkins/pipelines/" + p.getName() + "/runs/" + r.getId() + "/");
        HashSet<String> commitIds = new HashSet<>();
        for (Object o : (ArrayList) runDetails.get("changeSet")) {
            commitIds.add(((Map) o).get("checkoutCount").toString());
        }
        assertEquals(3, ((ArrayList) runDetails.get("changeSet")).size());
        assertEquals(new HashSet<>(Arrays.asList("0", "1")), commitIds);
    }

    private int updateREADMECounter = 0;

    private void updateREADME(GitSampleRepoRule sampleRepo) throws Exception {
        updateREADMECounter++;
        sampleRepo.write("README.md", "README + " + updateREADMECounter + ":" + sampleRepo.toString());
        sampleRepo.git("add", "README.md");
        sampleRepo.git("commit", "--all", "--message=README + " + updateREADMECounter + ":" + sampleRepo.toString());

    }

    @Test
    @Issue("JENKINS-53019")
    public void changelogFromReplayDeleted() throws Exception {
        String jenkinsFile = Resources.toString(Resources.getResource(getClass(), "mulitpleScms.jenkinsfile"), Charsets.UTF_8).replaceAll("%REPO1%", sampleRepo1.toString()).replaceAll("%REPO2%", sampleRepo2.toString());

        WorkflowJob p = j.createProject(WorkflowJob.class, "project");
        p.setDefinition(new CpsFlowDefinition(jenkinsFile, true));
        p.save();

        sampleRepo1.init();
        sampleRepo2.init();

        j.assertBuildStatus(Result.SUCCESS, p.scheduleBuild2(0));

        updateREADME(sampleRepo1);
        updateREADME(sampleRepo2);
        TimeUnit.SECONDS.sleep( 1);
        updateREADME(sampleRepo2);
        TimeUnit.SECONDS.sleep(1);

        Run run2 = j.assertBuildStatus(Result.SUCCESS, p.scheduleBuild2(0));

        ReplayAction replayAction = run2.getAction(ReplayAction.class);
        Run run3 = j.assertBuildStatus(Result.SUCCESS, replayAction.run(replayAction.getOriginalScript(), replayAction.getOriginalLoadedScripts()));
        run2.delete();


        Map<String, Object> runDetails = get("/organizations/jenkins/pipelines/" + p.getName() + "/runs/" + run3.getId() + "/");
        assertEquals(0, ((ArrayList) runDetails.get("changeSet")).size());
    }
}

