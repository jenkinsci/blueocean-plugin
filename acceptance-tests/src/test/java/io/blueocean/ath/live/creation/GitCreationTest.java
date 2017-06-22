package io.blueocean.ath.live.creation;

import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BaseTest;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.Login;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.model.Pipeline;
import io.blueocean.ath.pages.blue.DashboardPage;
import io.blueocean.ath.pages.blue.GitCreationPage;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

@Login
@RunWith(ATHJUnitRunner.class)
public class GitCreationTest extends BaseTest{

    @Inject @Named("live")
    Properties liveProperties;

    @Inject
    DashboardPage dashboardPage;
    @Inject
    GitCreationPage gitCreationPage;

    @Inject
    WaitUtil wait;

    @Test
    public void testSSHPrivateRepostory() throws IOException, GitAPIException, URISyntaxException {
        String gitUrl = liveProperties.getProperty("git.ssh.repository");
        String privateKeyFile = liveProperties.getProperty("git.ssh.keyfile");
        String pipelineName = liveProperties.getProperty("git.ssh.pipelineName");
        Assert.assertNotNull(gitUrl);
        Assert.assertNotNull(privateKeyFile);
        Assert.assertNotNull(pipelineName);

        String key = IOUtils.toString(new FileInputStream(privateKeyFile));

        MultiBranchPipeline pipeline = gitCreationPage.createPipeline(pipelineName, gitUrl, key, null, null);
        pipeline.getActivityPage().testNumberRunsComplete(1);
    }

    @Test
    public void testHttpsPrivateRepository() throws IOException, GitAPIException, URISyntaxException {
        String gitUrl = liveProperties.getProperty("git.https.repository");
        String user = liveProperties.getProperty("git.https.user");
        String pass = liveProperties.getProperty("git.https.pass");
        String pipelineName = liveProperties.getProperty("git.https.pipelineName");
        Assert.assertNotNull(gitUrl);
        Assert.assertNotNull(user);
        Assert.assertNotNull(pass);
        Assert.assertNotNull(pipelineName);
        Pipeline pipeline = gitCreationPage.createPipeline(pipelineName, gitUrl, null, user, pass);
        pipeline.getActivityPage().testNumberRunsComplete(1);
    }
}
