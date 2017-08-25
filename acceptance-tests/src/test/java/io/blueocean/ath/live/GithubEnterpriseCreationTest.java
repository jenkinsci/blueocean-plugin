package io.blueocean.ath.live;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.CustomJenkinsServer;
import io.blueocean.ath.Login;
import io.blueocean.ath.Retry;
import io.blueocean.ath.pages.blue.GithubAddServerDialogPage;
import io.blueocean.ath.pages.blue.GithubEnterpriseCreationPage;
import io.blueocean.ath.util.GithubHelper;
import io.blueocean.ath.util.WireMockBase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;
import java.io.IOException;


@Login
@RunWith(ATHJUnitRunner.class)
public class GithubEnterpriseCreationTest extends WireMockBase {

    private static GithubHelper githubHelper;
    private String repositoryName;

    @Rule
    public WireMockRule mockServer = createWireMockServerRule(
        "api/github-enterprise",
        "https://api.github.com/"
    );

    @Inject
    WebDriver driver;

    @Inject
    GithubEnterpriseCreationPage creationPage;

    @Inject
    GithubAddServerDialogPage dialog;

    @Inject
    CustomJenkinsServer jenkins;

    @BeforeClass
    public static void createGitHubHelper() {
        githubHelper = new GithubHelper();
    }

    @Before
    public void createEmptyRepository() throws IOException {
        repositoryName = githubHelper.createEmptyRepository();
    }

    @After
    public void cleanupRepository() throws IOException {
        githubHelper.cleanupRepository();
    }

    @Retry(3)
    @Test
    public void testGitHubEnterpriseCreation_addNewGitHubServer() throws IOException {
        String serverName = getServerNameUnique("My Server");
        String serverUrl = getServerUrl(mockServer);

        creationPage.beginCreationFlow(githubHelper.getOrganizationOrUsername());
        creationPage.clickAddServerButton();

        // "empty form" validation
        dialog.clickSaveServerButton();
        dialog.findFormErrorMessage("enter a name");
        dialog.findFormErrorMessage("enter a valid URL");

        dialog.enterServerName(serverName);
        // server-side URL validation (non-GitHub server)
        dialog.enterServerUrl("http://www.google.com");
        dialog.waitForErrorMessagesGone();
        dialog.clickSaveServerButton();
        dialog.findFormErrorMessage("Check hostname");
        // check GitHub server with invalid path
        dialog.enterServerUrl("https://github.beescloud.com");
        dialog.waitForErrorMessagesGone();
        dialog.clickSaveServerButton();
        dialog.findFormErrorMessage("Check path");

        // valid form data should submit
        dialog.enterServerUrl(serverUrl);
        dialog.waitForErrorMessagesGone();
        dialog.clickSaveServerButton();

        // As currently api.github.com may up in list thank to github branch source, this can mess up this test
        if (dialog.hasFormErrorMessage("already exists")) {
            // if we already have the "test" GHE (ie github cloud) - no worries, we wil cancel and use it
            dialog.clickCancelButton();
            creationPage.selectExistingServer();
        } else {
            dialog.wasDismissed();
        }


        creationPage.clickChooseServerNextStep();
        creationPage.completeCreationFlow(
            githubHelper.getAccessToken(),
            githubHelper.getOrganizationOrUsername(),
            repositoryName,
            true
        );
    }

    protected String getServerNameUnique(String name) {
        return name + " - " + GithubHelper.getRandomSuffix();
    }

    protected String getServerUrlUnique(String url) {
        return url + "?" + GithubHelper.getRandomSuffix();
    }

}
