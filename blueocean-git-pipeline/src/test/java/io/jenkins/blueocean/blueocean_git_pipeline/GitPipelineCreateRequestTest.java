package io.jenkins.blueocean.blueocean_git_pipeline;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.User;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import jenkins.branch.MultiBranchProject;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.plugins.git.traits.BranchDiscoveryTrait;
import jenkins.plugins.git.traits.CleanAfterCheckoutTrait;
import jenkins.plugins.git.traits.CleanBeforeCheckoutTrait;
import jenkins.plugins.git.traits.LocalBranchTrait;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMTrait;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GitPipelineCreateRequestTest extends PipelineBaseTest {

    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Before
    public void createSomeStuff() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", "pipeline { stage('Build 1') { steps { echo 'build' } } }");
        sampleRepo.write("file", "initial content");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--all", "--message=flow");
    }

    @Test
    public void createPipeline() throws UnirestException, IOException {
        User user = login("vivek", "Vivek Pandey", "vivek.pandey@gmail.com");
        Map r = new PipelineBaseTest.RequestBuilder(baseUrl)
            .status(201)
            .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
            .post("/organizations/jenkins/pipelines/")
            .data(ImmutableMap.of("name", "pipeline1",
                "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                "scmConfig", ImmutableMap.of("id", GitScm.ID, "uri", sampleRepo.toString())))
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
    }

}
