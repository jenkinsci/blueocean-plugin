package io.jenkins.blueocean.blueocean_git_pipeline;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.google.common.collect.Lists;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ErrorMessage.Error;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import io.jenkins.blueocean.scm.api.AbstractMultiBranchCreateRequest;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.SCMSource;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
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
    protected SCMSource createSource(@Nonnull MultiBranchProject project, @Nonnull BlueScmConfig scmConfig) {
        return new GitSCMSource(null, StringUtils.defaultString(scmConfig.getUri()), computeCredentialId(scmConfig), "*", "", false);
    }

    @Override
    protected List<Error> validate(String name, BlueScmConfig scmConfig) {
        List<Error> errors = Lists.newArrayList();
        if (scmConfig.getUri() == null) {
            errors.add(new ErrorMessage.Error("scmConfig.uri", ErrorMessage.Error.ErrorCodes.MISSING.toString(), "uri is required"));
        }else {
            StandardCredentials credentials = null;
            String credentialId = computeCredentialId(scmConfig);
            if(credentialId != null){
                credentials = GitUtils.getCredentials(Jenkins.getInstance(), scmConfig.getUri(), credentialId);
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
    protected String computeCredentialId(BlueScmConfig scmConfig) {
        return scmConfig.getCredentialId();
    }
}
