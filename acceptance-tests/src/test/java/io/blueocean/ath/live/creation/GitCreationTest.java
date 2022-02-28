package io.blueocean.ath.live.creation;

import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BlueOceanAcceptanceTest;
import io.blueocean.ath.Login;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.model.AbstractPipeline;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.DashboardPage;
import io.blueocean.ath.pages.blue.GitCreationPage;
import io.blueocean.ath.sse.SSEClientRule;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

@Login
@RunWith(ATHJUnitRunner.class)
public class GitCreationTest extends BlueOceanAcceptanceTest {
    private Logger logger = LoggerFactory.getLogger(GitCreationTest.class);

    @Inject
    @Named("live")
    Properties liveProperties;

    @Inject
    DashboardPage dashboardPage;
    @Inject
    GitCreationPage gitCreationPage;

    @Inject
    WaitUtil wait;

    @Inject
    @Rule
    public SSEClientRule sseClient;

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
        logger.info("PipelineNameHttps: " + pipelineName);
        logger.info("git repo - " + gitUrl);
        AbstractPipeline pipeline = gitCreationPage.createPipelinePW(sseClient, pipelineName, gitUrl, user, pass);
        pipeline.getActivityPage().testNumberRunsComplete(1);
    }

    @Ignore
    @Test
    public void testSSHPrivateRepository() throws IOException, GitAPIException, URISyntaxException {
        String gitUrl = liveProperties.getProperty("git.ssh.repository");
        String privateKeyFile = liveProperties.getProperty("git.ssh.keyfile");
        String pipelineName = liveProperties.getProperty("git.ssh.pipelineName");
        Assert.assertNotNull(gitUrl);
        Assert.assertNotNull(privateKeyFile);
        Assert.assertNotNull(pipelineName);
        logger.info("PipelineName: " + pipelineName);
        logger.info("git repo - " + gitUrl);
        String key = IOUtils.toString(new FileInputStream(privateKeyFile));

        MultiBranchPipeline pipeline = gitCreationPage.createPipelineSSH(sseClient, pipelineName, gitUrl, key);
        pipeline.getActivityPage().testNumberRunsComplete(1);
    }
}
