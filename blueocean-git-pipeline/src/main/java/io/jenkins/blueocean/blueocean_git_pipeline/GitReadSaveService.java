package io.jenkins.blueocean.blueocean_git_pipeline;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.kohsuke.stapler.StaplerRequest;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.plugins.git.GitException;
import hudson.plugins.git.GitTool;
import hudson.remoting.Base64;
import hudson.util.LogTaskListener;
import io.jenkins.blueocean.rest.impl.pipeline.ScmContentProvider;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.SCMSource;
import net.sf.json.JSONObject;

@Extension
public class GitReadSaveService extends ScmContentProvider {

    @Override
    public String getScmId() {
        return "git";
    }

    @Override
    public String getApiUrl(Item item) {
        return null;
    }
    
    public static class GitReadSaveRequest {
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
            this.contents = contents.clone(); // grr findbugs
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

    private GitReadSaveRequest makeSaveRequest(
            Item item, String readUrl, String writeUrl,
            String readCredentialId, String writeCredentialId,String branch, String commitMessage,
            String sourceBranch, String filePath, byte[] contents) throws IOException, InterruptedException {
        String existingReadUrl = readUrl;
        String existingWriteUrl = writeUrl;
        String existingReadCredentialId = readCredentialId;
        String existingWriteCredentialId = writeCredentialId;
        String defaultBranch = "master";
        GitTool gitTool = GitTool.getDefaultInstallation();
        GitSCMSource gitSource = null;
        if (item instanceof MultiBranchProject<?,?>) {
            MultiBranchProject<?,?> mbp = (MultiBranchProject<?,?>)item;
            for (SCMSource s : mbp.getSCMSources()) {
                if (s instanceof GitSCMSource) {
                    gitSource = (GitSCMSource)s;
                    // default write URL to the read URL, if no writable options are specified
                    existingReadUrl = existingWriteUrl = gitSource.getRemote();
                    existingReadCredentialId = existingWriteCredentialId = gitSource.getCredentialsId();
                    gitTool = Jenkins.getInstance().getDescriptorByType(GitTool.DescriptorImpl.class)
                            .getInstallation(gitSource.getGitTool());
                    if (gitTool == null) {
                        gitTool = GitTool.getDefaultInstallation();
                    }
                }
            }
        }
        if (false) {
            return new GitReadSaveRequest(
                StringUtils.defaultIfEmpty(readUrl, existingReadUrl),
                StringUtils.defaultIfEmpty(writeUrl, StringUtils.defaultIfEmpty(existingWriteUrl, readUrl)),
                StringUtils.defaultIfEmpty(readCredentialId, existingReadCredentialId),
                StringUtils.defaultIfEmpty(writeCredentialId, existingWriteCredentialId),
                StringUtils.defaultIfEmpty(branch, defaultBranch),
                commitMessage,
                StringUtils.defaultIfEmpty(sourceBranch, defaultBranch),
                filePath,
                contents,
                gitTool,
                gitSource
            );
        }
        return new GitSCMReadSaveService.GitSCMReadSaveRequest(
            item,
            StringUtils.defaultIfEmpty(readUrl, existingReadUrl),
            StringUtils.defaultIfEmpty(writeUrl, StringUtils.defaultIfEmpty(existingWriteUrl, readUrl)),
            StringUtils.defaultIfEmpty(readCredentialId, existingReadCredentialId),
            StringUtils.defaultIfEmpty(writeCredentialId, existingWriteCredentialId),
            StringUtils.defaultIfEmpty(branch, defaultBranch),
            commitMessage,
            StringUtils.defaultIfEmpty(sourceBranch, defaultBranch),
            filePath,
            contents,
            gitTool,
            gitSource
        );
    }

    private GitReadSaveRequest makeSaveRequest(Item item, StaplerRequest req) throws IOException, InterruptedException {
        String readUrl = req.getParameter("readUrl");
        return makeSaveRequest(
            item,
            readUrl,
            req.getParameter("writeUrl"),
            req.getParameter("readCredentialId"),
            req.getParameter("writeCredentialId"),
            req.getParameter("branch"),
            req.getParameter("commitMessage"),
            req.getParameter("sourceBranch"),
            req.getParameter("path"),
            Base64.decode(req.getParameter("contents"))
        );
    }

    private GitReadSaveRequest makeSaveRequest(Item item, JSONObject json) throws IOException, InterruptedException {
        JSONObject content = json.getJSONObject("content");
        return makeSaveRequest(
            item,
            null,
            null,
            null,
            null,
            content.getString("branch"),
            content.getString("message"),
            content.getString("sourceBranch"),
            content.getString("path"),
            Base64.decode(content.getString("base64Data"))
        );
    }

    @Override
    public Object getContent(StaplerRequest req, Item item) {
        GitReadSaveRequest r = null;
        try {
            r = makeSaveRequest(item, req);
            r.cloneRepo();
            String encoded = Base64.encode(r.read());
            return new GitFile(
                new GitContent(r.readUrl, r.readUrl, r.readUrl, r.filePath, 0, "sha", encoded, "", r.branch, r.sourceBranch, true)
            );
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (r != null) {
                r.cleanupRepo();
            }
        }
    }

    @Override
    public Object saveContent(StaplerRequest req, Item item) {
        GitReadSaveRequest r = null;
        try {
            // parse json...
            JSONObject json = JSONObject.fromObject(IOUtils.toString(req.getReader()));
            r = makeSaveRequest(item, json);
            r.cloneRepo();
            r.save();
            return new GitFile(
                new GitContent(r.readUrl, r.readUrl, r.readUrl, r.filePath, 0, "sha", null, "", r.branch, r.sourceBranch, true)
            );
        } catch (IOException | InterruptedException | GitException | URISyntaxException e) {
            throw new RuntimeException(e);
        } finally {
            if (r != null) {
                r.cleanupRepo();
            }
        }
    }

    @Override
    public boolean support(Item item) {
        if (item instanceof org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject) {
            MultiBranchProject<?,?> mbp = (MultiBranchProject<?,?>)item;
            SCMSource s = mbp.getSCMSources().get(0);
            if (s instanceof GitSCMSource) {
                return true;
            }
        }
        return false;
    }
}
