package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.jenkins.plugins.bitbucket.BranchDiscoveryTrait;
import com.cloudbees.jenkins.plugins.bitbucket.ForkPullRequestDiscoveryTrait;
import com.cloudbees.jenkins.plugins.bitbucket.OriginPullRequestDiscoveryTrait;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import jenkins.plugins.git.traits.CleanAfterCheckoutTrait;
import jenkins.plugins.git.traits.CleanBeforeCheckoutTrait;
import jenkins.plugins.git.traits.LocalBranchTrait;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.trait.SCMBuilder;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.api.trait.SCMTrait;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Vivek Pandey
 */
public class BitbucketPipelineCreateRequestTest extends BbServerWireMock {

    @Test
    public void createdWithTraits() throws Exception {
        String credentialId = createCredential(BitbucketServerScm.ID);
        Map r = new PipelineBaseTest.RequestBuilder(baseUrl)
            .status(201)
            .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
            .crumb( crumb )
            .post("/organizations/jenkins/pipelines/")
            .data(ImmutableMap.of(
                "name","pipeline1",
                "$class", "io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketPipelineCreateRequest",
                "scmConfig", ImmutableMap.of(
                    "id", BitbucketServerScm.ID,
                    "credentialId", credentialId,
                    "uri", apiUrl,
                    "config", ImmutableMap.of(
                        "repoOwner", "TESTP",
                        "repository", "pipeline-demo-test"
                    )
                )
            ))
            .build(Map.class);
        assertNotNull(r);
        assertEquals("pipeline1", r.get("name"));

        MultiBranchProject mbp = (MultiBranchProject) j.getInstance().getItem("pipeline1");
        BitbucketSCMSource source = (BitbucketSCMSource) mbp.getSCMSources().get(0);
        List<SCMSourceTrait> traits = source.getTraits();

        Assert.assertNotNull(SCMTrait.find(traits, CleanAfterCheckoutTrait.class));
        Assert.assertNotNull(SCMTrait.find(traits, CleanBeforeCheckoutTrait.class));
        Assert.assertNotNull(SCMTrait.find(traits, LocalBranchTrait.class));

        BranchDiscoveryTrait branchDiscoveryTrait = SCMTrait.find(traits, BranchDiscoveryTrait.class);
        Assert.assertNotNull(branchDiscoveryTrait);
        Assert.assertTrue(branchDiscoveryTrait.isBuildBranch());
        Assert.assertTrue(branchDiscoveryTrait.isBuildBranchesWithPR());

        ForkPullRequestDiscoveryTrait forkPullRequestDiscoveryTrait = SCMTrait.find(traits, ForkPullRequestDiscoveryTrait.class);
        Assert.assertNotNull(forkPullRequestDiscoveryTrait);
        Assert.assertTrue(forkPullRequestDiscoveryTrait.getTrust() instanceof ForkPullRequestDiscoveryTrait.TrustTeamForks);
        Assert.assertEquals(1, forkPullRequestDiscoveryTrait.getStrategies().size());
        Assert.assertTrue(forkPullRequestDiscoveryTrait.getStrategies().contains(ChangeRequestCheckoutStrategy.MERGE));

        OriginPullRequestDiscoveryTrait originPullRequestDiscoveryTrait = SCMTrait.find(traits, OriginPullRequestDiscoveryTrait.class);
        Assert.assertNotNull(originPullRequestDiscoveryTrait);
        Assert.assertEquals(1, originPullRequestDiscoveryTrait.getStrategies().size());
        Assert.assertTrue(originPullRequestDiscoveryTrait.getStrategies().contains(ChangeRequestCheckoutStrategy.MERGE));
    }

    @Test
    public void createPipelineBitbucketServerWithCredentialId() throws UnirestException, IOException {
        String credentialId = createCredential(BitbucketServerScm.ID);
        Map r = new PipelineBaseTest.RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
                .crumb( crumb )
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of(
                    "name","pipeline1",
                    "$class", "io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketPipelineCreateRequest",
                    "scmConfig", ImmutableMap.of(
                        "id", BitbucketServerScm.ID,
                        "credentialId", credentialId,
                        "uri", apiUrl,
                        "config", ImmutableMap.of(
                            "repoOwner", "TESTP",
                            "repository", "pipeline-demo-test"
                        )
                    )
                ))
                .build(Map.class);
        assertNotNull(r);
        assertEquals("pipeline1", r.get("name"));
    }

    @Test
    public void createPipelineBitbucketServerWithoutCredentialId() throws UnirestException, IOException {
        createCredential(BitbucketServerScm.ID);
        Map r = new PipelineBaseTest.RequestBuilder(baseUrl)
            .status(201)
            .jwtToken(getJwtToken(j.jenkins, authenticatedUser.getId(), authenticatedUser.getId()))
            .crumb( crumb )
            .post("/organizations/jenkins/pipelines/")
            .data(ImmutableMap.of(
                "name","pipeline1",
                "$class", "io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketPipelineCreateRequest",
                "scmConfig", ImmutableMap.of(
                    "id", BitbucketServerScm.ID,
                    "uri", apiUrl,
                    "config", ImmutableMap.of(
                        "repoOwner", "TESTP",
                        "repository", "pipeline-demo-test"
                    )
                )
            ))
            .build(Map.class);
        assertNotNull(r);
        assertEquals("pipeline1", r.get("name"));
    }
}
