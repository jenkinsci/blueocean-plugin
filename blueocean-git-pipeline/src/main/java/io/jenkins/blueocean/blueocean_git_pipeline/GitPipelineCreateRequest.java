package io.jenkins.blueocean.blueocean_git_pipeline;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ErrorMessage.Error;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import io.jenkins.blueocean.scm.api.AbstractMultiBranchCreateRequest;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.traits.BranchDiscoveryTrait;
import jenkins.plugins.git.traits.CleanAfterCheckoutTrait;
import jenkins.plugins.git.traits.CleanBeforeCheckoutTrait;
import jenkins.plugins.git.traits.LocalBranchTrait;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceTrait;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class GitPipelineCreateRequest extends AbstractMultiBranchCreateRequest {

    @DataBoundConstructor
    public GitPipelineCreateRequest(String name, BlueScmConfig scmConfig) {
        super(name, scmConfig);
    }

    @Override
    protected SCMSource createSource(@NonNull MultiBranchProject project, @NonNull BlueScmConfig scmConfig) {
        GitSCMSource gitSource = new GitSCMSource(StringUtils.defaultString(scmConfig.getUri()));
        gitSource.setCredentialsId(computeCredentialId(scmConfig));
        List<SCMSourceTrait> traits = gitSource.getTraits();
        traits.add(new BranchDiscoveryTrait());
        traits.add(new CleanBeforeCheckoutTrait());
        traits.add(new CleanAfterCheckoutTrait());
        traits.add(new LocalBranchTrait());
        return gitSource;
    }

    @Override
    protected List<Error> validate(String name, BlueScmConfig scmConfig) {
        List<Error> errors = new ArrayList<>();
        if (scmConfig.getUri() == null) {
            errors.add(new ErrorMessage.Error("scmConfig.uri", ErrorMessage.Error.ErrorCodes.MISSING.toString(), "uri is required"));
        }else {
            StandardCredentials credentials = null;
            String credentialId = computeCredentialId(scmConfig);
            if(credentialId != null){
                credentials = GitUtils.getCredentials(Jenkins.get(), scmConfig.getUri(), credentialId);
                if (credentials == null) {
                    errors.add(new ErrorMessage.Error("scmConfig.credentialId",
                        ErrorMessage.Error.ErrorCodes.NOT_FOUND.toString(),
                        String.format("credentialId: %s not found", credentialId)));
                }
            }
            //validate credentials if no credential id (perhaps git repo doesn't need auth or credentials is present)
            if(credentialId == null || credentials != null) {
                errors.addAll(GitUtils.validateCredentials(scmConfig.getUri(), credentials));
            }
        }
        return errors;
    }

    @Override
    protected boolean repoHasJenkinsFile(@NonNull SCMSource scmSource) {
        return true;
    }

    @Override
    protected String computeCredentialId(BlueScmConfig scmConfig) {
        return scmConfig.getCredentialId();
    }
}
