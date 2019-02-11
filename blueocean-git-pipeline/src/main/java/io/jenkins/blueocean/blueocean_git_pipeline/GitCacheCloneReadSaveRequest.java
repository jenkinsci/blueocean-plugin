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
import hudson.plugins.git.GitException;
import hudson.remoting.VirtualChannel;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.ssh.UserSSHKeyManager;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.plugins.git.GitSCMFileSystem;
import jenkins.scm.api.SCMFileSystem;
import jenkins.scm.api.SCMHead;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.plugins.gitclient.RepositoryCallback;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;

/**
 * Uses the SCM Git cache with a local clone to load/save content
 * @author kzantow
 */
class GitCacheCloneReadSaveRequest extends GitReadSaveRequest {
    private static final String LOCAL_REF_BASE = "refs/heads/";
    private static final String REMOTE_REF_BASE = "refs/heads/";

    public GitCacheCloneReadSaveRequest(AbstractGitSCMSource gitSource, String branch, String commitMessage, String sourceBranch, String filePath, byte[] contents) {
        super(gitSource, branch, commitMessage, sourceBranch, filePath, contents);
    }

    @Override
    byte[] read() throws IOException {
        return invokeOnScm(new GitSCMFileSystem.FSFunction<byte[]>() {
            @Override
            public byte[] invoke(Repository repository) throws IOException, InterruptedException {
                Git activeRepo = getActiveRepository(repository);
                Repository repo = activeRepo.getRepository();
                File repoDir = repo.getDirectory().getParentFile();
                FileInputStream fis = null;
                try {
                    File f = new File(repoDir, filePath);
                    if (f.canRead()) {
                        fis = new FileInputStream(f);
                        return IOUtils.toByteArray(fis);
                    }
                    return null;
                } finally {
                    if (fis != null) {
                        fis.close();
                    }
                    FileUtils.deleteDirectory(repoDir);
                }
            }
        });
    }

    @Override
    void save() throws IOException {
        invokeOnScm(new GitSCMFileSystem.FSFunction<Void>() {
            @Override
            public Void invoke(Repository repository) throws IOException, InterruptedException {
                Git activeRepo = getActiveRepository(repository);
                Repository repo = activeRepo.getRepository();
                File repoDir = repo.getDirectory().getParentFile();
                log.fine("Repo cloned to: " + repoDir.getCanonicalPath());
                try {
                    File f = new File(repoDir, filePath);
                    if (!f.exists() || f.canWrite()) {
                        try (Writer w = new OutputStreamWriter(new FileOutputStream(f), "utf-8")) {
                            w.write(new String(contents, "utf-8"));
                        }

                        try {
                            AddCommand add = activeRepo.add();
                            add.addFilepattern(filePath);
                            add.call();

                            CommitCommand commit = activeRepo.commit();
                            commit.setMessage(commitMessage);
                            commit.call();

                            // Push the changes
                            GitUtils.push(gitSource.getRemote(), repo, getCredential(), LOCAL_REF_BASE + sourceBranch, REMOTE_REF_BASE + branch);
                        } catch (GitAPIException ex) {
                            throw new ServiceException.UnexpectedErrorException(ex.getMessage(), ex);
                        }

                        return null;
                    }
                    throw new ServiceException.UnexpectedErrorException("Unable to write " + filePath);
                } finally {
                    FileUtils.deleteDirectory(repoDir);
                }
            }
        });
    }

    <T> T invokeOnScm(final GitSCMFileSystem.FSFunction<T> function) throws IOException {
        try {
            GitSCMFileSystem fs = getFilesystem();
            if (fs == null) {
                // Fall back to a git clone if we can't get the repository filesystem
                GitCloneReadSaveRequest gitClone = new GitCloneReadSaveRequest(gitSource, branch, commitMessage, sourceBranch, filePath, contents);
                GitClient git = gitClone.cloneRepo();
                try {
                    return git.withRepository(new RepositoryCallbackToFSFunctionAdapter<>(function));
                } finally {
                    gitClone.cleanupRepo();
                }
            }
            return fs.invoke(function);
        } catch (InterruptedException ex) {
            throw new ServiceException.UnexpectedErrorException("Unable to save " + filePath, ex);
        }
    }

    private GitSCMFileSystem getFilesystem() throws IOException, InterruptedException {
        try {
            return (GitSCMFileSystem) SCMFileSystem.of(gitSource, new SCMHead(sourceBranch));
        } catch(NullPointerException e) {
            // If the repository is totally empty with no commits, it
            // results in a NPE during the SCMFileSystem.of call
            return null;
        } catch(GitException e) {
            // TODO localization?
            if (e.getMessage().contains("Permission denied")) {
                throw new ServiceException.UnauthorizedException("Not authorized", e);
            }
            throw e;
        }
    }

    private @Nonnull Git getActiveRepository(Repository repository) throws IOException {
        try {
            // Clone the bare repository
            File cloneDir = File.createTempFile("clone", "");

            if (cloneDir.exists()) {
                if (cloneDir.isDirectory()) {
                    FileUtils.deleteDirectory(cloneDir);
                } else {
                    if (!cloneDir.delete()) {
                        throw new ServiceException.UnexpectedErrorException("Unable to delete repository clone");
                    }
                }
            }
            if (!cloneDir.mkdirs()) {
                throw new ServiceException.UnexpectedErrorException("Unable to create repository clone directory");
            }

            String url = repository.getConfig().getString( "remote", "origin", "url" );
            Git gitClient = Git.cloneRepository()
                .setCloneAllBranches(false)
                .setProgressMonitor(new CloneProgressMonitor(url))
                .setURI(repository.getDirectory().getCanonicalPath())
                .setDirectory(cloneDir)
                .call();

            RemoteRemoveCommand remove = gitClient.remoteRemove();
            remove.setName("origin");
            remove.call();

            RemoteAddCommand add = gitClient.remoteAdd();
            add.setName("origin");
            add.setUri(new URIish(gitSource.getRemote()));
            add.call();

            if (GitUtils.isSshUrl(gitSource.getRemote())) {
                // Get committer info and credentials
                User user = User.current();
                if (user == null) {
                    throw new ServiceException.UnauthorizedException("Not authenticated");
                }
                BasicSSHUserPrivateKey privateKey = UserSSHKeyManager.getOrCreate(user);

                // Make sure up-to-date and credentials work
                GitUtils.fetch(repository, privateKey);
            } else {
                FetchCommand fetch = gitClient.fetch();
                fetch.call();
            }

            if (!StringUtils.isEmpty(sourceBranch) && !sourceBranch.equals(branch)) {
                CheckoutCommand checkout = gitClient.checkout();
                checkout.setStartPoint("origin/" + sourceBranch);
                checkout.setName(sourceBranch);
                checkout.setCreateBranch(true); // to create a new local branch
                checkout.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.NOTRACK);
                checkout.call();

                checkout = gitClient.checkout();
                checkout.setName(branch);
                checkout.setCreateBranch(true); // this *should* be a new branch
                checkout.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.NOTRACK);
                checkout.call();
            } else {
                CheckoutCommand checkout = gitClient.checkout();
                checkout.setStartPoint("origin/" + branch);
                checkout.setName(branch);
                checkout.setCreateBranch(true); // to create a new local branch
                checkout.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.NOTRACK);
                checkout.call();
            }

            return gitClient;
        } catch (GitAPIException | URISyntaxException ex) {
            throw new ServiceException.UnexpectedErrorException("Unable to get working repository directory", ex);
        }
    }

    static class RepositoryCallbackToFSFunctionAdapter<T> implements RepositoryCallback<T> {
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
