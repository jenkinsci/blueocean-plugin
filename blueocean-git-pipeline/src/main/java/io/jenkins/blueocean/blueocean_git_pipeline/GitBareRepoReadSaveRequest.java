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
import hudson.EnvVars;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.plugins.git.GitException;
import hudson.plugins.git.GitTool;
import hudson.remoting.VirtualChannel;
import hudson.tasks.MailAddressResolver;
import hudson.util.LogTaskListener;
import io.jenkins.blueocean.commons.ServiceException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.plugins.git.GitSCMFileSystem;
import jenkins.plugins.git.traits.GitToolSCMSourceTrait;
import jenkins.scm.api.SCMFileSystem;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.trait.SCMSourceTrait;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.plugins.gitclient.RepositoryCallback;

/**
 * Uses the SCM Git cache operating on the bare repository to load/save content
 * "as efficiently as possible"
 * @author kzantow
 */
class GitBareRepoReadSaveRequest extends GitReadSaveRequest {
    private static final String LOCAL_REF_BASE = "refs/remotes/origin/";
    private static final String REMOTE_REF_BASE = "refs/heads/";

    private final File repositoryPath;
    private final GitTool gitTool;

    GitBareRepoReadSaveRequest(AbstractGitSCMSource gitSource, String branch, String commitMessage, String sourceBranch, String filePath, byte[] contents) {
        super(gitSource, branch, commitMessage, sourceBranch, filePath, contents);
        GitTool.DescriptorImpl toolDesc = Jenkins.get().getDescriptorByType( GitTool.DescriptorImpl.class);
        @SuppressWarnings("deprecation")
        GitTool foundGitTool = null;
        if (gitSource != null) {
            for ( SCMSourceTrait trait : gitSource.getTraits()) {
                if (trait instanceof GitToolSCMSourceTrait ) {
                    foundGitTool = toolDesc.getInstallation(((GitToolSCMSourceTrait) trait).getGitTool());
                }
            }
        }

        if (foundGitTool == null) {
            foundGitTool = GitTool.getDefaultInstallation();
        }

        this.gitTool = foundGitTool;
        try {
            repositoryPath = Files.createTempDirectory( "git").toFile();
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Unable to create working directory for repository clone");
        }
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

                    if(mailAddress == null) {
                        mailAddress = user.getId() + "@email-address-not-set";
                    }

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

    <T> T invokeOnScm(final GitSCMFileSystem.FSFunction<T> function) throws IOException {
        try {
            GitSCMFileSystem fs = getFilesystem();
            if (fs == null) {
                // Fall back to a git clone if we can't get the repository filesystem
                GitClient git = cloneRepo();
                try {
                    return git.withRepository(new RepositoryCallbackToFSFunctionAdapter<>( function));
                } finally {
                    cleanupRepo();
                }
            }
            return fs.invoke(function);
        } catch (InterruptedException ex) {
            throw new ServiceException.UnexpectedErrorException("Unable to save " + filePath, ex);
        }
    }

    private GitSCMFileSystem getFilesystem() throws IOException, InterruptedException {
        try {
            return (GitSCMFileSystem) SCMFileSystem.of( gitSource, new SCMHead( sourceBranch));
        } catch(NullPointerException e) {
            // If the repository is totally empty with no commits, it
            // results in a NPE during the SCMFileSystem.of call
            return null;
        } catch( GitException e) {
            // TODO localization?
            if (e.getMessage().contains("Permission denied")) {
                throw new ServiceException.UnauthorizedException("Not authorized", e);
            }
            throw e;
        }
    }

    GitClient cloneRepo() throws InterruptedException, IOException {
        EnvVars environment = new EnvVars();
        TaskListener taskListener = new LogTaskListener( Logger.getAnonymousLogger(), Level.ALL);
        String gitExe = gitTool.getGitExe();
        GitClient git = org.jenkinsci.plugins.gitclient.Git.with( taskListener, environment)
            .in(repositoryPath)
            .using(gitExe)
            .getClient();

        git.addCredentials(gitSource.getRemote(), getCredential());

        try {
            git.clone(gitSource.getRemote(), "origin", true, null);

            log.fine("Repository " + gitSource.getRemote() + " cloned to: " + repositoryPath.getCanonicalPath());
        } catch(GitException e) {
            // check if this is an empty repository
            boolean isEmptyRepo = false;
            try {
                if (git.getRemoteReferences(gitSource.getRemote(), null, true, false).isEmpty()) {
                    isEmptyRepo = true;
                }
            } catch(GitException ge) {
                // *sigh* @ this necessary hack; {@link org.jenkinsci.plugins.gitclient.CliGitAPIImpl#getRemoteReferences}
                if ("unexpected ls-remote output ".equals(ge.getMessage())) { // blank line, command succeeded
                    isEmptyRepo = true;
                }
                // ignore other reasons
            }

            if(isEmptyRepo) {
                git.init();
                git.addRemoteUrl("origin", gitSource.getRemote());

                log.fine("Repository " + gitSource.getRemote() + " not found, created new to: " + repositoryPath.getCanonicalPath());
            } else {
                throw e;
            }
        }

        return git;
    }

    void cleanupRepo() {
        try {
            FileUtils.deleteDirectory( repositoryPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class RepositoryCallbackToFSFunctionAdapter<T> implements RepositoryCallback<T>
    {
        private final GitSCMFileSystem.FSFunction<T> function;

        public RepositoryCallbackToFSFunctionAdapter(GitSCMFileSystem.FSFunction<T> function) {
            this.function = function;
        }

        @Override
        public T invoke(Repository repo, VirtualChannel channel) throws IOException, InterruptedException {
            return function.invoke(repo);
        }
    }

}
