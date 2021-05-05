package io.blueocean.ath.offline.personalization;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.offbytwo.jenkins.model.Crumb;
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
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

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

    protected ResourceResolver resources;

    abstract protected Logger getLogger();

    @Before
    public void setUp() throws Exception {
        resources = new ResourceResolver(getClass());

        String user = "alice";
        getLogger().info(String.format("deleting any existing favorites for {}", user));

        String path = base + "/crumbIssuer/api/json";

        Crumb crumb = Unirest.get(path).
            basicAuth(user, user).
            asObject(Crumb.class).
            getBody();


        path = base + "/users/" + user + "/favorites/";
        HttpResponse<String> response =
            Unirest.delete(path)
                .basicAuth(user, user)
                .header(crumb.getCrumbRequestField(), crumb.getCrumb())
                .asString();

        getLogger().info("delete favorites, res status: {}", response.getStatus());

    }

    @After
    public void tearDown() throws IOException {
        // wipe out all jobs to avoid causing issues w/ SearchTest
        jobApi.deleteFolder(FOLDER);
    }
}
