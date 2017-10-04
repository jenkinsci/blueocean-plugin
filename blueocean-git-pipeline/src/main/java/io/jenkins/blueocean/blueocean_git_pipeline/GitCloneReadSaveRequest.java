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

import hudson.EnvVars;
import hudson.model.TaskListener;
import hudson.plugins.git.GitException;
import hudson.plugins.git.GitTool;
import hudson.util.LogTaskListener;
import io.jenkins.blueocean.commons.ServiceException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.plugins.git.traits.GitToolSCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTrait;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;

/**
 * Basic functional read/save using a clone of the remote
 * @author kzantow
 */
class GitCloneReadSaveRequest extends GitReadSaveRequest {
    private final File repositoryPath;
    private final GitTool gitTool;

    public GitCloneReadSaveRequest(AbstractGitSCMSource gitSource, String branch, String commitMessage, String sourceBranch, String filePath, byte[] contents) {
        super(gitSource, branch, commitMessage, sourceBranch, filePath, contents);

        GitTool.DescriptorImpl toolDesc = Jenkins.getInstance().getDescriptorByType(GitTool.DescriptorImpl.class);
        @SuppressWarnings("deprecation")
        GitTool foundGitTool = null;
        for (SCMSourceTrait trait : gitSource.getTraits()) {
            if (trait instanceof GitToolSCMSourceTrait) {
                foundGitTool = toolDesc.getInstallation(((GitToolSCMSourceTrait) trait).getGitTool());
            }
        }
        if (foundGitTool == null) {
            foundGitTool = GitTool.getDefaultInstallation();
        }

        this.gitTool = foundGitTool;
        try {
            repositoryPath = Files.createTempDirectory("git").toFile();
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Unable to create working directory for repository clone");
        }
    }

    GitClient cloneRepo() throws InterruptedException, IOException {
        EnvVars environment = new EnvVars();
        TaskListener taskListener = new LogTaskListener(Logger.getAnonymousLogger(), Level.ALL);
        String gitExe = gitTool.getGitExe();
        GitClient git = Git.with(taskListener, environment)
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

    @Override
    byte[] read() throws IOException {
        try {
            GitClient git = cloneRepo();
            try {
                // thank you test for how to use something...
                // https://github.com/jenkinsci/git-client-plugin/blob/master/src/test/java/org/jenkinsci/plugins/gitclient/GitClientTest.java#L1108
                git.checkoutBranch(branch, "origin/" + branch);
            } catch(Exception e) {
                throw new RuntimeException("Branch not found: " + branch);
            }
            File f = new File(repositoryPath, filePath);
            if (f.canRead()) {
                return FileUtils.readFileToByteArray(f);
            }
            return null;
        } catch (InterruptedException ex) {
            throw new ServiceException.UnexpectedErrorException("Unable to read " + filePath, ex);
        } finally {
            cleanupRepo();
        }
    }

    @Override
    void save() throws IOException {
        try {
            GitClient git = cloneRepo();
            try {
                git.checkoutBranch(sourceBranch, "origin/" + sourceBranch);
            } catch(Exception e) {
                throw new RuntimeException("Branch not found: " + sourceBranch);
            }
            if (!sourceBranch.equals(branch)) {
                //git.branch(branch);
                git.checkoutBranch(branch, "origin/" + sourceBranch);
            }
            File f = new File(repositoryPath, filePath);
            // commit will fail if the contents hasn't changed
            if (!f.exists() || !Arrays.equals(FileUtils.readFileToByteArray(f), contents)) {
                FileUtils.writeByteArrayToFile(f, contents);
                git.add(filePath);
                git.commit(commitMessage);
            }
            git.push().ref(branch).to(new URIish(gitSource.getRemote())).execute();
        } catch (InterruptedException | GitException | URISyntaxException ex) {
            throw new ServiceException.UnexpectedErrorException("Unable to save " + filePath, ex);
        } finally {
            cleanupRepo();
        }
    }

    void cleanupRepo() {
        try {
            FileUtils.deleteDirectory(repositoryPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
