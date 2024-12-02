package io.jenkins.blueocean.preload;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FavoriteListStatePreloaderTest extends PipelineBaseTest {

    @Rule
    public GitSampleRepoRule masterRepo = new GitSampleRepoRule();
    @Rule
    public GitSampleRepoRule mainRepo = new GitSampleRepoRule();
    @Rule
    public GitSampleRepoRule noFavoriteRepo = new GitSampleRepoRule();

    @Before
    public void setup() throws Exception {
        super.setup();
        setupScm();
        invalidateBranchMetadataCache();
    }

    @Test
    public void simpleTest() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());

        registerProject("p-master", "master", masterRepo);
        registerProject("p-main", "main", mainRepo);
        registerProject("p-no-favorite", "master", noFavoriteRepo);

        j.waitUntilNoActivity();

        String token = getJwtToken(j.jenkins, "alice", "alice");

        favoriteBranch("p-master", "feature2", token);
        favoriteBranch("p-master", "feature4", token);
        favoriteBranch("p-master", "master", token);
        favoriteBranch("p-main", "main", token);

        HttpResponse<String> response = new RequestBuilder(j.jenkins.getRootUrl())
            .get("/blue/pipelines")
            .jwtToken(token)
            .build()
            .asString();

        Document doc = Jsoup.parse(response.getBody());
        String script = doc.select("head script#blueocean-page-state-preload-decorator-data").html().toString();
        JSONObject json = JSONObject.fromObject(script);

        Assert.assertTrue(json.containsKey("favoritesList"));
        JSONArray favoritesList = json.getJSONArray("favoritesList");

        final String data = favoritesList.toString();
        Assert.assertTrue("master branch not include or not primary", data.contains("{\"name\":\"p-master/master\",\"primary\":true}"));
        Assert.assertTrue("feature2 branch not include or primary", data.contains("{\"name\":\"p-master/feature2\",\"primary\":false}"));
        Assert.assertTrue("feature4 branch not include or primary", data.contains("{\"name\":\"p-master/feature4\",\"primary\":false}"));
        Assert.assertTrue("main branch not include or not primary", data.contains("{\"name\":\"p-main/main\",\"primary\":true}"));

        Assert.assertFalse("feature1 branch should not be included", data.contains("{\"name\":\"p-master/feature1\",\"primary\":false}"));
        Assert.assertFalse("p-no-favorite should not be included ", data.contains("{\"name\":\"p-no-favorite/master\",\"primary\":true}"));
    }

    private void favoriteBranch(String project, String branch, String token) {
        new RequestBuilder(baseUrl)
            .put("/organizations/jenkins/pipelines/" + project + "/branches/" + branch + "/favorite")
            .jwtToken(token)
            .data(ImmutableMap.of("favorite", true))
            .build(Map.class);
    }

    private void registerProject(String name, String branchName, GitSampleRepoRule repo) throws Exception {
        WorkflowMultiBranchProject mp = j.jenkins.createProject(WorkflowMultiBranchProject.class, name);
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, repo.toString(), "", "*", "", false),
            new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        for (SCMSource source : mp.getSCMSources()) {
            assertEquals(mp, source.getOwner());
        }
        scheduleAndFindBranchProject(mp, branchName);
    }

    private void setupScm() throws Exception {
        initRepo(masterRepo, "master");
        initRepo(mainRepo, "main");
        initRepo(noFavoriteRepo, "master");

        createBranch("feature1");
        createBranch("feature2");
        createBranch("feature4");
    }

    private void createBranch(String name) throws Exception {
        masterRepo.git("branch", name);
    }

    private void initRepo(GitSampleRepoRule sampleRepo, String primaryBranchName) throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", "stage('build') {\n " + "node {echo 'Building'}}\n" +
            "stage('test') {\nnode { echo 'Testing'}}\n" +
            "stage('deploy') {\nnode { echo 'Deploying'}}\n"
        );
        sampleRepo.write("file", "initial content");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--all", "--message=flow");
        sampleRepo.git("branch", "-m", "master", primaryBranchName);
    }
}
