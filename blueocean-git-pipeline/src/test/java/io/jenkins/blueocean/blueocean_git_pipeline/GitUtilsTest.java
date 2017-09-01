package io.jenkins.blueocean.blueocean_git_pipeline;

import com.google.common.io.Files;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import jenkins.plugins.git.GitSampleRepoRule;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

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
    public void testOperations() throws Exception {
        String repoUrl = repo.getRoot().getCanonicalPath();
        File cloneDir = Files.createTempDir();
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
}
