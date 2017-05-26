package io.blueocean.ath;


import com.google.common.collect.ContiguousSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


public class GitRepositoryRule extends ExternalResource {

    @Rule
    private TemporaryFolder temporaryFolder;

    public File gitDirectory;

    public Git git;

    @Override
    protected void before() throws Throwable {
        temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        gitDirectory = temporaryFolder.newFolder();
        git = Git.init().setDirectory(gitDirectory).call();
        logger.info("Created git repository at " + gitDirectory.getAbsolutePath());

    }

    @Override
    protected void after() {
        //temporaryFolder.delete();
    }

    private Logger logger = Logger.getLogger(GitRepositoryRule.class);

    @NotNull
    public List<Ref> createBranches(@NotNull  String prefix, int number) throws GitAPIException {
        List<Ref> refs = Lists.newArrayList();
        for(int i = 1; i < number + 1; i++) {
            Ref ref = git.branchCreate().setName(prefix + i).call();
            refs.add(ref);
        }
        logger.info("Created " + number + " branches " + prefix + "[1-" + (number + 1) + "]");

        return refs;
    }
}
