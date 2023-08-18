package io.jenkins.blueocean.blueocean_git_pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import hudson.model.User;
import io.jenkins.blueocean.commons.MapsHelper;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import java.util.List;
import java.util.Map;
import jenkins.branch.MultiBranchProject;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.plugins.git.traits.BranchDiscoveryTrait;
import jenkins.plugins.git.traits.CleanAfterCheckoutTrait;
import jenkins.plugins.git.traits.CleanBeforeCheckoutTrait;
import jenkins.plugins.git.traits.LocalBranchTrait;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMTrait;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;

public class GitPipelineCreateRequestTest extends PipelineBaseTest {

    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @ClassRule
    public static BuildWatcher watcher = new BuildWatcher();

    @Before
    public void createSomeStuff() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", "pipeline { agent any; stages { stage('Build 1') { steps { echo 'build' } } } }");
        sampleRepo.write("file", "initial content");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--all", "--message=flow");
    }

    @Test
    public void createPipeline() throws Exception {
        j.jenkins.setQuietPeriod(0);
        User user = login("vivek", "Vivek Pandey", "vivek.pandey@gmail.com");
        Map r = new PipelineBaseTest.RequestBuilder(baseUrl)
            .status(201)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .crumb( crumb )
            .post("/organizations/jenkins/pipelines/")
            .data( MapsHelper.of("name", "pipeline1",
                                 "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                                 "scmConfig",
                                 MapsHelper.of("id", GitScm.ID, "uri", sampleRepo.toString())))
            .build(Map.class);
        assertNotNull(r);
        assertEquals("pipeline1", r.get("name"));

        MultiBranchProject mbp = (MultiBranchProject) j.getInstance().getItem("pipeline1");
        GitSCMSource source = (GitSCMSource) mbp.getSCMSources().get(0);
        List<SCMSourceTrait> traits = source.getTraits();

        Assert.assertNotNull(SCMTrait.find(traits, BranchDiscoveryTrait.class));
        Assert.assertNotNull(SCMTrait.find(traits, CleanAfterCheckoutTrait.class));
        Assert.assertNotNull(SCMTrait.find(traits, CleanBeforeCheckoutTrait.class));
        Assert.assertNotNull(SCMTrait.find(traits, LocalBranchTrait.class));
        j.waitUntilNoActivity();
        j.waitForCompletion(j.jenkins.getItemByFullName("pipeline1/master", WorkflowJob.class).getBuildByNumber(1));
    }

}
