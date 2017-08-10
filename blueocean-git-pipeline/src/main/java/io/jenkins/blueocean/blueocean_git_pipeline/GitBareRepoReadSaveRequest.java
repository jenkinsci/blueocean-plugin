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

import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import hudson.model.User;
import hudson.tasks.MailAddressResolver;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.service.embedded.util.UserSSHKeyManager;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.plugins.git.GitSCMFileSystem;
import jenkins.plugins.git.GitSCMSource;
import org.eclipse.jgit.api.Git;
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

    public GitBareRepoReadSaveRequest(GitSCMSource gitSource, String branch, String commitMessage, String sourceBranch, String filePath, byte[] contents) {
        super(gitSource, branch, commitMessage, sourceBranch, filePath, contents);
    }

    @Override
    byte[] read() throws IOException {
        try {
            GitSCMFileSystem fs = getFilesystem();
            return fs.invoke(new GitSCMFileSystem.FSFunction<byte[]>() {
                @Override
                public byte[] invoke(Repository repository) throws IOException, InterruptedException {
                    try (Git git = new Git(repository)) {
                        return GitUtils.readFile(repository, LOCAL_REF_BASE + branch, filePath);
                    }
                }
            });
        } catch (InterruptedException ex) {
            throw new ServiceException.UnexpectedErrorException("Unable to read " + filePath, ex);
        }
    }

    @Override
    void save() throws IOException {
        try {
            GitSCMFileSystem fs = getFilesystem();
            fs.invoke(new GitSCMFileSystem.FSFunction<Void>() {
                @Override
                public Void invoke(Repository repo) throws IOException, InterruptedException {
                    String localBranchRef = LOCAL_REF_BASE + sourceBranch;

                    ObjectId branchHead = repo.resolve(localBranchRef);

                    try {
                        // Get committer info and credentials
                        User user = User.current();
                        if (user == null) {
                            throw new ServiceException.UnauthorizedException("Not authenticated");
                        }
                        String mailAddress = MailAddressResolver.resolve(user);
                        BasicSSHUserPrivateKey privateKey = UserSSHKeyManager.getOrCreate(user);

                        // Make sure up-to-date and credentials work
                        GitUtils.fetch(repo, privateKey);

                        GitUtils.commit(repo, localBranchRef, filePath, contents, user.getId(), mailAddress, commitMessage, TimeZone.getDefault(), new Date());

                        GitUtils.push(gitSource, repo, privateKey, localBranchRef, REMOTE_REF_BASE + branch);
                        return null;
                    } catch (RuntimeException e) {
                        // if anything bad happened, roll back
                        try {
                            RefUpdate rollback = repo.updateRef(localBranchRef);
                            rollback.setNewObjectId(branchHead);
                            rollback.forceUpdate();
                        } catch(Exception ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unable to roll back repo after save failure", ex);
                            throw new ServiceException.UnexpectedErrorException("Unable to roll back repo after save failure", e);
                        }
                        throw e;
                    }
                }
            });
        } catch (InterruptedException ex) {
            throw new ServiceException.UnexpectedErrorException("Unable to save " + filePath, ex);
        }
    }
}
