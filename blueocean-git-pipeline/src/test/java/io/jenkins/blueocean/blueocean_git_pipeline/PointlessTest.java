package io.jenkins.blueocean.blueocean_git_pipeline;

import jenkins.plugins.git.GitSCMFileSystem;
import org.eclipse.jgit.lib.Repository;
import org.junit.Test;

import java.io.IOException;

public class PointlessTest {
    @Test
    public void coverage() throws IOException, InterruptedException {
        new GitCacheCloneReadSaveRequest.RepositoryCallbackToFSFunctionAdapter<>(new GitSCMFileSystem.FSFunction<Object>() {
            @Override
            public Object invoke(Repository repository) throws IOException, InterruptedException {
                return null;
            }
        }).invoke(null, null);
    }
}
