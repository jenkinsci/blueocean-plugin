package io.blueocean.ath;


import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.junit.JGitTestUtil;
import org.eclipse.jgit.lib.Ref;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
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

    private Logger logger = Logger.getLogger(GitRepositoryRule.class);

    @NotNull
    public List<Ref> createBranches(@NotNull String prefix, int number) throws GitAPIException {
        List<Ref> refs = Lists.newArrayList();
        for(int i = 1; i < number + 1; i++) {
            Ref ref = client.branchCreate().setName(prefix + i).call();
            refs.add(ref);
        }
        logger.info("Created " + number + " branches " + prefix + "[1-" + (number + 1) + "]");

        return refs;
    }

    public Ref createBranch(String branch) throws GitAPIException {
        Ref ref = client.branchCreate().setName(branch).call();
        logger.info("Created branch " + branch);
        return ref;
    }

    public void writeJenkinsFile(String jenkinsFile) throws IOException {
        JGitTestUtil.writeTrashFile(client.getRepository(), "Jenkinsfile", jenkinsFile);
        logger.info("Wrote Jenkinsfile to git repository");
    }
}
