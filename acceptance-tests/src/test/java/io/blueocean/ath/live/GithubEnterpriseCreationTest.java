package io.blueocean.ath.live;

import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.CustomJenkinsServer;
import io.blueocean.ath.Login;
import io.blueocean.ath.pages.blue.GithubAddServerDialogPage;
import io.blueocean.ath.pages.blue.GithubEnterpriseCreationPage;
import io.blueocean.ath.util.GithubHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;
import java.io.IOException;


@Login
@RunWith(ATHJUnitRunner.class)
public class GithubEnterpriseCreationTest {

    private static GithubHelper githubHelper;
    private String repositoryName;

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


    @Test
    public void testGitHubEnterpriseCreation_addNewGitHubServer() throws IOException {
        String serverName = getServerNameUnique("My Server");
        String serverUrl = getServerUrlUnique("https://api.github.com");

        creationPage.beginCreationFlow(githubHelper.getOrganizationOrUsername());
        creationPage.clickAddServerButton();

        // "empty form" validation
        dialog.clickSaveServerButton();
        dialog.findFormErrorMessage("enter a name");
        dialog.findFormErrorMessage("enter a valid URL");
        // server-side URL validation
        dialog.enterServerName(serverName);
        dialog.enterServerUrl("foo");
        dialog.waitForErrorMessagesGone();
        dialog.clickSaveServerButton();
        dialog.findFormErrorMessage("Could not connect");
        // valid form data should submit

        dialog.enterServerUrl(serverUrl);
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
