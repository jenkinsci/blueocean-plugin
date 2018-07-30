package io.blueocean.ath.offline.personalization;

import io.blueocean.ath.*;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.factory.ClassicPipelineFactory;
import io.blueocean.ath.factory.FreestyleJobFactory;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.Folder;
import io.blueocean.ath.pages.blue.FavoritesDashboardPage;
import io.jenkins.blueocean.util.HttpRequest;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import javax.inject.Inject;
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

    @Inject
    JenkinsUser user;

    protected ResourceResolver resources;

    abstract protected Logger getLogger();

    private HttpRequest httpRequest() {
        return new HttpRequest(base + "/blue/rest");
    }

    @Before
    public void setUp() throws IOException {
        resources = new ResourceResolver(getClass());


        getLogger().info(String.format("deleting any existing favorites for %s", user));

        httpRequest()
            .Delete("/users/{user}/favorites/")
            .urlPart("user", user.username)
            .auth(user.username, user.password)
            .status(204)
            .as(Void.class);
    }

    @After
    public void tearDown() throws IOException {
        // wipe out all jobs to avoid causing issues w/ SearchTest
        jobApi.deleteFolder(FOLDER);
    }
}
