package io.blueocean.ath.offline.personalization;

import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.Login;
import io.blueocean.ath.ResourceResolver;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.factory.ClassicPipelineFactory;
import io.blueocean.ath.factory.FreestyleJobFactory;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.Folder;
import io.blueocean.ath.pages.blue.FavoritesDashboardPage;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;

/**
 * @author cliffmeyers
 */
@Login
@RunWith(ATHJUnitRunner.class)
abstract public class AbstractFavoritesTest implements WebDriverMixin {

    static final Folder FOLDER = new Folder("personalization-folder");

    @Rule @Inject
    public GitRepositoryRule git;

    @Inject @BaseUrl
    String base;

    @Inject
    ClassicJobApi jobApi;

    @Inject
    FreestyleJobFactory freestyleFactory;

    @Inject
    ClassicPipelineFactory pipelineFactory;

    @Inject
    MultiBranchPipelineFactory multibranchFactory;

    @Inject
    FavoritesDashboardPage dashboardPage;

    protected ResourceResolver resources;

    abstract protected Logger getLogger();

    @Before
    public void setUp() throws IOException {
        resources = new ResourceResolver(getClass());

        String user = "alice";
        getLogger().info(String.format("deleting any existing favorites for %s", user));

        Client restClient = ClientBuilder.newClient().register(HttpAuthenticationFeature.basic(user, user));
        restClient.target(base + "/users/" + user + "/favorites/").request().delete();
    }

    @After
    public void tearDown() throws IOException {
        // wipe out all jobs to avoid causing issues w/ SearchTest
        jobApi.deleteFolder(FOLDER);
    }
}
