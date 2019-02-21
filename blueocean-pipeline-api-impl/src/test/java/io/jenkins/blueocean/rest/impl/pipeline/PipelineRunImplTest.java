package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import hudson.model.Run;
import jenkins.plugins.git.GitSampleRepoRule;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

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

        Run r = p.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(r);

        updateREADME(sampleRepo1);
        updateREADME(sampleRepo2);
        updateREADME(sampleRepo2);

        r = p.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(r);

        System.out.println("----");
        System.out.println(jenkinsFile);
        System.out.println("----");

        Map<String, Object> runDetails = get("/organizations/jenkins/pipelines/" + p.getName() + "/runs/" + r.getId() + "/");
        HashSet<String> commitIds = new HashSet<>();
        for (Object o : (ArrayList) runDetails.get("changeSet")) {
            commitIds.add(((Map) o).get("checkoutCount").toString());
        }
        assertEquals(commitIds, new HashSet<>(Arrays.asList("0", "1")));
    }

    private int updateREADMECounter = 0;
    private void updateREADME(GitSampleRepoRule sampleRepo) throws Exception {
        updateREADMECounter++;
        sampleRepo.write("README.md", "README + " + updateREADMECounter + ":" + sampleRepo.toString());
        sampleRepo.git("add", "README.md");
        sampleRepo.git("commit", "--all", "--message=README + " + updateREADMECounter + ":" + sampleRepo.toString());

    }
}
