package io.blueocean.ath;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.junit.JGitTestUtil;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class GitRepositoryRule extends ExternalResource {

    @Rule
    private TemporaryFolder temporaryFolder;

    public File gitDirectory;

    public Git client;

    @Override
    protected void before() throws Throwable {
        temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        gitDirectory = temporaryFolder.newFolder();
        client = Git.init().setDirectory(gitDirectory).call();
        logger.info("Created git repository at " + gitDirectory.getAbsolutePath());

    }

    @Override
    protected void after() {
        //temporaryFolder.delete();
    }

    private Logger logger = LoggerFactory.getLogger(GitRepositoryRule.class);

    @NotNull
    public List<Ref> createBranches(@NotNull String prefix, int number) throws GitAPIException {
        List<Ref> refs = new ArrayList<>();
        for(int i = 1; i < number + 1; i++) {
            Ref ref = client.branchCreate().setName(prefix + i).call();
            refs.add(ref);
        }
        logger.info("Created " + number + " branches " + prefix + "[1-" + (number + 1) + "]");

        return refs;
    }

    public Ref createBranch(String branch) throws GitAPIException {
        Ref ref = client.branchCreate().setName(branch).call();
        logger.info("Created branch {}", branch);
        return ref;
    }

    public void writeJenkinsFile(String jenkinsFile) throws IOException {
        JGitTestUtil.writeTrashFile(client.getRepository(), "Jenkinsfile", jenkinsFile);
        logger.info("Wrote Jenkinsfile to git repository");
    }

    public void writeFile(String name, String contents) throws IOException {
        JGitTestUtil.writeTrashFile(client.getRepository(), name, contents);
        logger.info("Wrote {} to git repository", name);
    }

    public RevCommit commit(String message) throws GitAPIException {
        return client.commit().setMessage(message).call();
    }

    public void addAll() throws GitAPIException {
        client.add().addFilepattern(".").call();
    }

}
