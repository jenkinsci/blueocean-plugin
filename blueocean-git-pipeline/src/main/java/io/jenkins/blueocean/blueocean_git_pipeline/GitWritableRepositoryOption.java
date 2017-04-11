package io.jenkins.blueocean.blueocean_git_pipeline;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.plugins.git.extensions.GitSCMExtensionDescriptor;

/**
 * In order to be able to write back to repositories, we may need
 * to specify additional properties with an alternate URL and/or
 * credential ID.
 * 
 * @author kzantow
 */
public class GitWritableRepositoryOption extends GitSCMExtension {

    private String repositoryUrl;
    private String credentialId;
    private String remoteName;

    @DataBoundConstructor
    public GitWritableRepositoryOption(String repositoryUrl, String credentialId, String remoteName) {
        this.repositoryUrl = repositoryUrl;
        this.credentialId = credentialId;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public String getRemoteName() {
        return remoteName;
    }

    @Override
    public boolean requiresWorkspaceForPolling() {
        return false;
    }

    @Extension
    public static class DescriptorImpl extends GitSCMExtensionDescriptor {
        @Override
        public String getDisplayName() {
            return "Writable Repository Options";
        }
    }
}
