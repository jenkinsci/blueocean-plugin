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
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.plugins.git.GitSCMSource;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;

/**
 * Basic functional read/save using a clone of the remote
 * @author kzantow
 */
public class GitReadSaveRequest  {
    final String readUrl;
    final String writeUrl;
    final String readCredentialId;
    final String writeCredentialId;
    final String branch;
    final String commitMessage;
    final String sourceBranch;
    final String filePath;
    final byte[] contents;
    final GitClient git;
    final GitTool gitTool;
    final File repositoryPath;
    final GitSCMSource gitSource;

    public GitReadSaveRequest(
            String readUrl, String writeUrl,
            String writeCredentialId, String readCredentialId,
            String branch, String commitMessage,
            String sourceBranch, String filePath,
            byte[] contents, GitTool gitTool, GitSCMSource gitSource)
            throws IOException, InterruptedException {
        this.readUrl = readUrl;
        this.writeUrl = writeUrl;
        this.readCredentialId = readCredentialId;
        this.writeCredentialId = writeCredentialId;
        this.branch = branch;
        this.commitMessage = commitMessage;
        this.sourceBranch = sourceBranch;
        this.filePath = filePath;
        this.contents = contents == null ? null : contents.clone(); // grr findbugs
        this.gitTool = gitTool;
        this.gitSource = gitSource;
        this.repositoryPath = Files.createTempDirectory("git").toFile();

        EnvVars environment = new EnvVars();
        TaskListener taskListener = new LogTaskListener(Logger.getAnonymousLogger(), Level.ALL);
        String gitExe = gitTool.getGitExe();
        git = Git.with(taskListener, environment)
                .in(repositoryPath)
                .using(gitExe)
                .getClient();
    }

    byte[] read() throws IOException, InterruptedException {
        try {
            // thank you test for how to use something...
            // https://github.com/jenkinsci/git-client-plugin/blob/master/src/test/java/org/jenkinsci/plugins/gitclient/GitClientTest.java#L1108
            git.checkoutBranch(branch, "origin/" + branch);
        } catch(Exception e) {
            throw new RuntimeException("Branch not found: " + branch);
        }
        return FileUtils.readFileToByteArray(new File(repositoryPath, filePath));
    }

    void cloneRepo() throws IOException, InterruptedException {
        git.clone(readUrl, "origin", true, null);
        git.addRemoteUrl("write", writeUrl);
    }

    void save() throws IOException, InterruptedException, GitException, URISyntaxException {
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
        git.push().ref(branch).to(new URIish(writeUrl)).execute();
    }

    void cleanupRepo() {
        try {
            FileUtils.deleteDirectory(repositoryPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
