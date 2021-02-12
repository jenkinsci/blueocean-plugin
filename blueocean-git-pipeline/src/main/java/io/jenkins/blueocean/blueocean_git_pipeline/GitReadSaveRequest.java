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
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.ssh.UserSSHKeyManager;
import jenkins.plugins.git.AbstractGitSCMSource;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author kzantow
 */
abstract class GitReadSaveRequest  {
    final static Logger log = Logger.getLogger(GitReadSaveRequest.class.getName());

    final AbstractGitSCMSource gitSource;
    final String branch;
    final String commitMessage;
    final String sourceBranch;
    final String filePath;
    final byte[] contents;

    GitReadSaveRequest(
            AbstractGitSCMSource gitSource,
            String branch, String commitMessage,
            String sourceBranch, String filePath,
            byte[] contents) {
        this.gitSource = gitSource;
        this.branch = branch;
        this.commitMessage = commitMessage;
        this.sourceBranch = sourceBranch;
        this.filePath = filePath;
        this.contents = contents == null ? null : contents.clone(); // grr findbugs
    }

    @CheckForNull
    StandardCredentials getCredential() {
        StandardCredentials credential = null;

        User user = User.current();
        if (user == null) {
            throw new ServiceException.UnauthorizedException("Not authenticated");
        }

        // Get committer info and credentials
        if (GitUtils.isSshUrl(gitSource.getRemote()) || GitUtils.isLocalUnixFileUrl(gitSource.getRemote())) {
            credential = UserSSHKeyManager.getOrCreate(user);
        } else {
            String credentialId = GitScm.makeCredentialId(gitSource.getRemote());

            if (credentialId != null) {
                credential = CredentialsUtils.findCredential(credentialId,
                                                             StandardCredentials.class,
                                                             new BlueOceanDomainRequirement());
            }
        }
        return credential;
    }

    /**
     * Reads the file contents as a byte array
     * @return contents
     * @throws IOException if IO exception
     */
    abstract byte[] read() throws IOException;

    /**
     * Saves the file contents back to the source repo
     * @throws IOException if IO exception
     */
    abstract void save() throws IOException;
}
