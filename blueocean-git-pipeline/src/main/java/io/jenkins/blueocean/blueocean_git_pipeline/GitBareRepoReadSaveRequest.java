/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.blueocean.blueocean_git_pipeline;

import hudson.model.Item;
import hudson.plugins.git.GitException;
import hudson.plugins.git.GitTool;
import io.jenkins.blueocean.commons.ServiceException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.TimeZone;
import jenkins.plugins.git.GitSCMFileSystem;
import jenkins.plugins.git.GitSCMSource;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;

/**
 * Uses the SCM Git cache operating on the bare repository to load/save content
 * "as efficiently as possible"
 * @author kzantow
 */
public class GitBareRepoReadSaveRequest extends GitCacheCloneReadSaveRequest {
    private static final String LOCAL_REF_BASE = "refs/remotes/origin/";
    private static final String REMOTE_REF_BASE = "refs/heads/";

    public GitBareRepoReadSaveRequest(Item item, String readUrl, String writeUrl, String writeCredentialId, String readCredentialId, String branch, String commitMessage, String sourceBranch, String filePath, byte[] contents, GitTool gitTool, GitSCMSource gitSource) throws IOException, InterruptedException {
        super(item, readUrl, writeUrl, writeCredentialId, readCredentialId, branch, commitMessage, sourceBranch, filePath, contents, gitTool, gitSource);
    }

    @Override
    byte[] read() throws IOException, InterruptedException {
        GitSCMFileSystem fs = getFilesystem();
        return fs.invoke(new GitSCMFileSystem.FSFunction<byte[]>() {
            @Override
            public byte[] invoke(Repository repository) throws IOException, InterruptedException {
                try (Git git = new Git(repository)) {
                    return GitUtils.readFile(repository, LOCAL_REF_BASE + branch, filePath);
                }
            }
        });
    }

    @Override
    void save() throws IOException, InterruptedException, GitException, URISyntaxException {
        GitSCMFileSystem fs = getFilesystem();
        fs.invoke(new GitSCMFileSystem.FSFunction<Void>() {
            @Override
            public Void invoke(Repository db) throws IOException, InterruptedException {
                String localBranchRef = LOCAL_REF_BASE + sourceBranch;
                
                // TODO: rollback on fail
                // TODO: correct credentials
                // TODO: author and commiter
                GitUtils.commit(db, localBranchRef, filePath, contents, "Jenkins", "jenkins@jenkins.x", commitMessage, TimeZone.getDefault(), new Date());
                
                String remote = readUrl;

                try (Git git = new Git(db)) {
                    String pushSpec = "+" + localBranchRef + ":" + REMOTE_REF_BASE + branch;
                    Iterable<PushResult> resultIterable = git.push()
                        //.setCredentialsProvider(credentialsProvider)
                        .setRefSpecs(new RefSpec(pushSpec))
                        .setRemote(remote)
                        .call();
                    PushResult result = resultIterable.iterator().next();
                    if (result.getRemoteUpdates().isEmpty()) {
                        throw new RuntimeException("No remote updates");
                    }
                } catch (GitAPIException ex) {
                    throw new ServiceException.UnexpectedErrorException("Unable to save and push Jenkinsfile: " + ex.getMessage(), ex);
                }
                return null;
            }
        });
    }
}
