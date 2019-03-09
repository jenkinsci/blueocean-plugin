package io.blueocean.ath.live;

import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.CustomJenkinsServer;
import io.blueocean.ath.Login;
import io.blueocean.ath.Retry;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.pages.blue.DashboardPage;
import io.blueocean.ath.pages.blue.GithubCreationPage;
import io.blueocean.ath.pages.blue.PullRequestsPage;
import io.blueocean.ath.sse.SSEClientRule;
import io.blueocean.ath.util.GithubHelper;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.github.GHContentUpdateResponse;

import javax.inject.Inject;
import java.io.IOException;

@Login
@RunWith(ATHJUnitRunner.class)
public class GithubCreationTest {
    private Logger logger = Logger.getLogger(GithubCreationTest.class);

    @Inject
    GithubCreationPage creationPage;

    @Inject
    MultiBranchPipelineFactory mbpFactory;

    @Inject @Rule
    public SSEClientRule sseClient;

    @Inject DashboardPage dashboardPage;

    @Inject
    CustomJenkinsServer jenkins;

    @Inject
    GithubHelper helper;

    @Inject
    ClassicJobApi jobApi;

    /**
     * Cleans up repository after the test has completed.
     *
     * @throws IOException
     */
    @After
    public void deleteRepository() throws IOException {
        helper.cleanupRepository();
    }

    /**
     * Clean up the job so it can't conflict/mess up other tests
     */
    @After
    public void deletePipeline() throws IOException {
        jobApi.deletePipeline(helper.getActualRepositoryName());
    }

    /**
     * Every test in this class gets a blank github repository created for them.
     *
     * @throws IOException
     */
    @Before
    public void createBlankRepo() throws IOException {
        helper.createEmptyRepository();
    }

    /**
     * This test tests the github creation flow.
     *
     * Creates a github repo with a sample Jenkinsfile
     *
     */
    @Test
    @Retry(3)
    public void testCreatePipelineFull() throws IOException {
        byte[] content = "stage('build') { echo 'yes' }".getBytes("UTF-8");
        GHContentUpdateResponse updateResponse = helper.getGithubRepository().createContent(content, "Jenkinsfile", "Jenkinsfile", "master");
        helper.getGithubRepository().createRef("refs/heads/branch1", updateResponse.getCommit().getSHA1());
        logger.info("Created master and branch1 branches in " + helper.getGithubRepository().getFullName());
        helper.getGithubRepository().createContent("hi there","newfile", "newfile", "branch1");

        creationPage.createPipeline(helper.getAccessToken(), helper.getOrganizationOrUsername(), helper.getActualRepositoryName());
    }

    /**
     * This test walks through a simple Pull Request flow..
     *
     * -Creates a github repo with a sample Jenkinsfile and follows
     *  the typical create flow
     * -Navigates back to the top level Dashboard page
     * -Creates a PR in the GH repo
     * -Triggers a rescan of the multibranch pipeline
     * -Opens the PR tab to verify that we actually have something there.
     *
     */
    @Test
    @Retry(3)
    public void testCreatePullRequest() throws IOException {
        String branchToCreate = "new-branch";
        String commitMessage = "Add new-file to our repo";
        MultiBranchPipeline pipeline = mbpFactory.pipeline(helper.getActualRepositoryName());
        byte[] firstJenkinsfile = "stage('first-build') { echo 'first-build' }".getBytes("UTF-8");
        GHContentUpdateResponse initialUpdateResponse = helper.getGithubRepository().createContent(firstJenkinsfile, "firstJenkinsfile", "Jenkinsfile", "master");
        helper.getGithubRepository().createRef(("refs/heads/" + branchToCreate), initialUpdateResponse.getCommit().getSHA1());
        logger.info("Created master and " + branchToCreate + " branches in " + helper.getActualRepositoryName());

        helper.getGithubRepository().createContent("hi there","newfile", "new-file", branchToCreate);
        creationPage.createPipeline(helper.getAccessToken(), helper.getOrganizationOrUsername(), helper.getActualRepositoryName());
        dashboardPage.open();
        helper.getGithubRepository().createPullRequest(
            commitMessage,
            branchToCreate,
            "master",
            "My first pull request is very exciting.");
        // Fire the rescan.
        pipeline.rescanThisPipeline();
        dashboardPage.clickPipeline(helper.getActualRepositoryName());
        ActivityPage activityPage = pipeline.getActivityPage().checkUrl();
        PullRequestsPage pullRequestsPage = activityPage.clickPullRequestsTab();
        pullRequestsPage.clickRunButton("1");
        pullRequestsPage.clickHistoryButton("1");
        // We'll be on the activity page now, pre-filtered to PR-1.
        // Go to the Pull Requests tab one last time.
        activityPage.clickPullRequestsTab();
    }

    @Test
    @Retry(3)
    public void testTokenValidation_failed() throws IOException {
        jenkins.deleteUserDomainCredential("alice", "blueocean-github-domain", "github");
        creationPage.navigateToCreation();
        creationPage.selectGithubCreation();
        creationPage.validateGithubOauthToken("foo");
        creationPage.findFormErrorMessage("Invalid access token.");
    }
}
