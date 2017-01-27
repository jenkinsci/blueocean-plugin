package io.jenkins.blueocean.blueocean_git_pipeline;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.EnvVars;
import hudson.model.TaskListener;
import hudson.plugins.git.GitException;
import hudson.security.ACL;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.scm.api.SCMSourceOwner;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * @author Vivek Pandey
 */
class GitUtils {
    /**
     *  Calls 'git ls-remote -h uri' to check if git uri or supplied credentials are valid
     *
     * @param owner SCM owner, such as MultiBranchProject
     * @param uri git repo uri
     * @param credentialId credential id to use when accessing git
     */
    static void validateCredentials(@Nonnull SCMSourceOwner owner, @Nonnull String uri, @Nullable String credentialId){
        StandardUsernameCredentials credentials = null;
        if(credentialId != null) {
            credentials = getCredentials(owner, uri, credentialId);
            if (credentials == null) {
                throw new ServiceException.BadRequestExpception(new ErrorMessage(400, "Failed to create Git pipeline")
                        .add(new ErrorMessage.Error("scmConfig.credentialId",
                                ErrorMessage.Error.ErrorCodes.NOT_FOUND.toString(),
                                String.format("credentialId: %s not found", credentialId))));
            }
        }
        Git git = new Git(TaskListener.NULL, new EnvVars());
        try {
            GitClient gitClient = git.getClient();
            if(credentials != null) {
                gitClient.addCredentials(uri, credentials);
            }
            gitClient.getRemoteReferences(uri,null, true,false);
        } catch (IOException | InterruptedException e) {
            throw  new ServiceException.UnexpectedErrorException("Failed to create pipeline due to unexpected error: "+e.getMessage(), e);
        } catch (GitException e){
            throw new ServiceException.BadRequestExpception(new ErrorMessage(400, "Failed to create Git pipeline")
                    .add(new ErrorMessage.Error("scmConfig.uri",
                            ErrorMessage.Error.ErrorCodes.INVALID.toString(),
                            "Invalid uri: " + uri)), e);
        } catch (IllegalStateException e){
            throw new ServiceException.ForbiddenException(new ErrorMessage(403, "Failed to create Git pipeline")
                    .add(new ErrorMessage.Error("scmConfig.credentialId",
                            ErrorMessage.Error.ErrorCodes.INVALID.toString(),
                            "Invalid credentialId: " + credentialId)), e);
        }
    }

    private static StandardUsernameCredentials getCredentials(SCMSourceOwner owner, String uri, String credentialId){
        return CredentialsMatchers
                .firstOrNull(
                        CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class, owner,
                                ACL.SYSTEM, URIRequirementBuilder.fromUri(uri).build()),
                        CredentialsMatchers.allOf(CredentialsMatchers.withId(credentialId),
                                GitClient.CREDENTIALS_MATCHER));
    }
}
