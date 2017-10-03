package io.blueocean.ath.offline;

import com.cdancy.bitbucket.rest.BitbucketClient;
import com.cdancy.bitbucket.rest.domain.project.Project;
import com.cdancy.bitbucket.rest.domain.project.ProjectPage;
import com.cdancy.bitbucket.rest.options.CreateRepository;
import com.google.inject.Inject;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.Login;
import io.blueocean.ath.pages.blue.BitbucketCreationPage;
import org.junit.Test;
import org.junit.runner.RunWith;

@Login
@RunWith(ATHJUnitRunner.class)
public class BitbucketServerTest {

    @Inject
    BitbucketCreationPage creationPage;

    @Test
    public void testJenkinsfileCreate() {
        BitbucketClient client = BitbucketClient.builder()
            .endPoint("http://127.0.0.1:7990/bitbucket")
            .credentials("admin:admin").build();
        String project = "PROJECT_1";
        String repo = "testJenkinsfileCreate";
        client.api().repositoryApi().delete(project, repo);
        client.api().repositoryApi().create(project, CreateRepository.create(repo, true));

        creationPage.navigateToCreation();
        creationPage.selectBitbucketServerCreation();
        creationPage.clickAddServer();
        creationPage.enterServerDetials("bbserver", "http://localhost:7990/bitbucket");
        creationPage.enterServerCredentials("admin", "admin");
    }
}
