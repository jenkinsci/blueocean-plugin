package io.jenkins.blueocean.blueocean_git_pipeline;

import hudson.model.User;
import io.jenkins.blueocean.commons.MapsHelper;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import jenkins.plugins.git.GitSampleRepoRule;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

public class GitUtilsTest extends PipelineBaseTest {
    @Rule
    public GitSampleRepoRule repo = new GitSampleRepoRule();

    private final String initialText = "initial-text";

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        // create git repo
        repo.init();
        repo.write("test.txt", initialText);
        repo.git("add", "test.txt");
        repo.git("commit", "--all", "--message=" + initialText);
    }

    @Test
    public void testSshUrlChecker() {
        Assert.assertTrue(GitUtils.isSshUrl("ssh://some-host/some-path"));
        Assert.assertTrue(GitUtils.isSshUrl("ssh://some-host/some-path/more"));
        Assert.assertTrue(GitUtils.isSshUrl("ssh://some-host:port/some-path/more"));
        Assert.assertTrue(GitUtils.isSshUrl("ssh://user@some-host/some-path/more"));
        Assert.assertTrue(GitUtils.isSshUrl("ssh://user@some-host:port/some-path/more"));
        Assert.assertTrue(GitUtils.isSshUrl("user@some-host:some-path"));
        Assert.assertTrue(GitUtils.isSshUrl("user@some-host:some-path/more"));
        Assert.assertTrue(!GitUtils.isSshUrl("proto://user@some-host/some-path"));
        Assert.assertTrue(!GitUtils.isSshUrl("proto://user@some-host:port/some-path"));
        Assert.assertTrue(!GitUtils.isSshUrl("proto://user@some-host/some-path/more"));
        Assert.assertTrue(!GitUtils.isSshUrl("proto://user@some-host:port/some-path/more"));
        Assert.assertTrue(!GitUtils.isSshUrl("https://user@some-host/some-path"));
        Assert.assertTrue(!GitUtils.isSshUrl("https://user@some-host:port/some-path"));
        Assert.assertTrue(!GitUtils.isSshUrl("https://user@some-host/some-path/more"));
        Assert.assertTrue(!GitUtils.isSshUrl("https://user@some-host:port/some-path/more"));
    }

    @Test
    public void testOperations() throws Exception {
        String repoUrl = repo.getRoot().getCanonicalPath();
        File cloneDir = Files.createTempDirectory("bo_test").toFile();
        Git gitClient = Git.cloneRepository()
            .setCloneAllBranches(false)
            .setURI(repoUrl)
            .setDirectory(cloneDir)
            .call();

        Repository cloneRepo = gitClient.getRepository();

        File file = new File(cloneDir, "test.txt");
        String text = FileUtils.readFileToString(file, "utf-8");

        Assert.assertTrue(initialText.equals(text));

        final String firstText = "first-text";
        repo.write("test.txt", firstText);
        repo.git("add", "test.txt");
        repo.git("commit", "--all", "--message=" + firstText);

        GitUtils.fetch(cloneRepo, null);
        GitUtils.merge(cloneRepo, "master", "refs/remotes/origin/master");

        // Check that the wc was updated
        text = FileUtils.readFileToString(file, "utf-8");
        Assert.assertTrue(firstText.equals(text));

        // Update the file locally
        final String secondText = "second-text";
        FileUtils.write(file, secondText);

        text = FileUtils.readFileToString(file, "utf-8");
        Assert.assertTrue(secondText.equals(text));

        gitClient.add().addFilepattern("test.txt").call();
        gitClient.commit().setMessage(secondText).call();
        GitUtils.push(repoUrl, cloneRepo, null, "refs/heads/master", "refs/heads/master");

        // Assert the remote was updated
        repo.git("reset", "--hard", "refs/heads/master"); // get the latest content
        text = FileUtils.readFileToString(new File(repo.getRoot(), "test.txt"), "utf-8");
        Assert.assertTrue(secondText.equals(text));
    }

    @Test
    public void testValidatePushAccessFails() throws Exception {
        User user = login();
        this.jwtToken = getJwtToken(j.jenkins, user.getId(), user.getId());

        Map resp = new RequestBuilder(baseUrl)
            .status(200)
            .get("/organizations/jenkins/user/publickey/").build(Map.class);

        String id = (String)resp.get("id");
        Assert.assertNotNull(id);

        final Map<String, Object> body = MapsHelper.of(
            "repositoryUrl", "git@github.com:vivek/capability-annotation.git",
            "credentialId", id,
            "requirePush", true,
            "branch", "master");

        post("/organizations/jenkins/scm/git/validate/", body, "application/json", 428);
    }
}
