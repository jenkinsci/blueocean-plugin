package io.jenkins.blueocean.blueocean_git_pipeline;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.EnvVars;
import hudson.model.ItemGroup;
import hudson.model.TaskListener;
import hudson.plugins.git.GitException;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.CredentialsUtils;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
class GitUtils {
    private static final Logger logger = LoggerFactory.getLogger(GitUtils.class);

    /**
     *  Calls 'git ls-remote -h uri' to check if git uri or supplied credentials are valid
     *
     * @param uri git repo uri
     * @param credentials credential to use when accessing git
     * @return list of Errors. Empty list means success.
     */
    static List<ErrorMessage.Error> validateCredentials(@Nonnull String uri, @Nullable StandardCredentials credentials) throws GitException{
        List<ErrorMessage.Error> errors = new ArrayList<>();
        Git git = new Git(TaskListener.NULL, new EnvVars());
        try {
            GitClient gitClient = git.getClient();
            if(credentials != null) {
                gitClient.addCredentials(uri, credentials);
            }
            gitClient.getRemoteReferences(uri,null, true,false);
        } catch (IOException | InterruptedException e) {
            logger.error("Error running git remote-ls: " + e.getMessage(), e);
            throw  new ServiceException.UnexpectedErrorException("Failed to create pipeline due to unexpected error: "+e.getMessage(), e);
        } catch (IllegalStateException | GitException e){
            logger.error("Error running git remote-ls: " + e.getMessage(), e);
            if(credentials != null) {
                // XXX: check for 'not authorized' is hack. Git plugin API (org.eclipse.jgit.transport.TransportHttp.connect())does not send
                //      back any error code so that we can distinguish between unauthorized vs bad url or some other type of errors.
                //      Where org.eclipse.jgit.transport.SshTransport.connect() throws IllegalStateException in case of unauthorized,
                //      org.eclipse.jgit.transport.HttpTransport.connect() throws TransportException with error code 'not authorized'
                //      appended to the message.
                if(e instanceof IllegalStateException || e.getMessage().endsWith("not authorized")){
                    errors.add(new ErrorMessage.Error("scmConfig.credentialId",
                                    ErrorMessage.Error.ErrorCodes.INVALID.toString(),
                                    "Invalid credentialId: " + credentials.getId()));
                }
            }else{
                errors.add(new ErrorMessage.Error("scmConfig.uri", ErrorMessage.Error.ErrorCodes.INVALID.toString(),
                        e.getMessage()));
            }
        }
        return errors;
    }

    static StandardCredentials getCredentials(ItemGroup owner, String uri, String credentialId){
        return CredentialsUtils.findCredential(credentialId, StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());
    }
}
