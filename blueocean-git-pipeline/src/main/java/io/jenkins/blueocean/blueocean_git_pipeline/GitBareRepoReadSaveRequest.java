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

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.model.User;
import hudson.tasks.MailAddressResolver;
import io.jenkins.blueocean.commons.ServiceException;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;

import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.plugins.git.GitSCMFileSystem;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;

/**
 * Uses the SCM Git cache operating on the bare repository to load/save content
 * "as efficiently as possible"
 * @author kzantow
 */
class GitBareRepoReadSaveRequest extends GitCacheCloneReadSaveRequest {
    private static final String LOCAL_REF_BASE = "refs/remotes/origin/";
    private static final String REMOTE_REF_BASE = "refs/heads/";

    GitBareRepoReadSaveRequest(AbstractGitSCMSource gitSource, String branch, String commitMessage, String sourceBranch, String filePath, byte[] contents) {
        super(gitSource, branch, commitMessage, sourceBranch, filePath, contents);
    }

    @Override
    byte[] read() throws IOException {
        return invokeOnScm(new GitSCMFileSystem.FSFunction<byte[]>() {
            @Override
            public byte[] invoke(Repository repo) throws IOException, InterruptedException {
                // Make sure credentials work
                GitUtils.validatePushAccess(repo, gitSource.getRemote(), getCredential());
                // Make sure up-to-date
                GitUtils.fetch(repo, getCredential());
                return GitUtils.readFile(repo, LOCAL_REF_BASE + branch, filePath);
            }
        });
    }

    @Override
    void save() throws IOException {
        invokeOnScm(new GitSCMFileSystem.FSFunction<Void>() {
            @Override
            public Void invoke(Repository repo) throws IOException, InterruptedException {
                String localBranchRef = LOCAL_REF_BASE + sourceBranch;

                ObjectId branchHead = repo.resolve(localBranchRef);

                try {
                    // Get committer info
                    User user = User.current();
                    if (user == null) {
                        throw new ServiceException.UnauthorizedException("Not authenticated");
                    }
                    String mailAddress = MailAddressResolver.resolve(user);
                    StandardCredentials credential = getCredential();

                    // Make sure up-to-date and credentials work
                    GitUtils.fetch(repo, credential);

                    GitUtils.commit(repo, localBranchRef, filePath, contents, user.getId(), mailAddress, commitMessage, TimeZone.getDefault(), new Date());

                    GitUtils.push(gitSource.getRemote(), repo, credential, localBranchRef, REMOTE_REF_BASE + branch);
                    return null;
                } finally {
                    // always roll back to undo our local changes
                    try {
                        if (branchHead != null) { // branchHead may be null if this was an empty repo
                            RefUpdate rollback = repo.updateRef(localBranchRef);
                            rollback.setNewObjectId(branchHead);
                            rollback.forceUpdate();
                        }
                    } catch(Exception ex) {
                        log.log(Level.SEVERE, "Unable to roll back repo after save failure", ex);
                    }
                }
            }
        });
    }
}
