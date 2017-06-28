package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.collect.Lists;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.BitbucketServerApi;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import io.jenkins.blueocean.scm.api.AbstractMultiBranchCreateRequest;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import jenkins.scm.api.SCMSource;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class BitbucketPipelineCreateRequest extends AbstractMultiBranchCreateRequest {
    @DataBoundConstructor
    public BitbucketPipelineCreateRequest(String name, BlueScmConfig scmConfig) {
        super(name, scmConfig);
    }

    @Override
    protected SCMSource createSource(@Nonnull MultiBranchProject project, @Nonnull BlueScmConfig scmConfig) {
        BitbucketSCMSource bitbucketSCMSource =  new BitbucketSCMSource(null, (String)scmConfig.getConfig().get("repoOwner"), (String)scmConfig.getConfig().get("repository"));
        bitbucketSCMSource.setCredentialsId(scmConfig.getCredentialId());

        //Set bb server uri only in case we are talking to BB server
        String bbUri = scmConfig.getUri();
        if(StringUtils.isNotBlank(bbUri)) {
            BitbucketApiFactory apiFactory = BitbucketApiFactory.resolve(bbUri);
            if(apiFactory instanceof BitbucketServerApi.BitbucketServerApiFactory){
                bitbucketSCMSource.setBitbucketServerUrl(scmConfig.getUri());
            }
        }

        //Setup Jenkins root url, if not set bitbucket cloud notification will fail
        JenkinsLocationConfiguration jenkinsLocationConfiguration = JenkinsLocationConfiguration.get();
        if(jenkinsLocationConfiguration != null) {
            String url = jenkinsLocationConfiguration.getUrl();
            if (url == null) {
                url = Jenkins.getInstance().getRootUrl();
                if (url != null) {
                    jenkinsLocationConfiguration.setUrl(url);
                }
            }
        }
        return bitbucketSCMSource;
    }

    @Override
    protected List<ErrorMessage.Error> validate(String name, BlueScmConfig scmConfig) {
        List<ErrorMessage.Error> errors = Lists.newArrayList();
        StandardUsernamePasswordCredentials credentials = null;
        String credentialId = scmConfig.getCredentialId();
        if(credentialId != null){
            credentials = CredentialsUtils.findCredential(credentialId, StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());
            if (credentials == null) {
                errors.add(new ErrorMessage.Error("scmConfig.credentialId",
                                ErrorMessage.Error.ErrorCodes.NOT_FOUND.toString(),
                                "No Credentials instance found for credentialId: " + credentialId));
            }
        }
        if(StringUtils.isBlank((String)scmConfig.getConfig().get("repoOwner"))){
            errors.add(new ErrorMessage.Error("scmConfig.repoOwner",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(),
                    "repoOwner is required"));
        }

        if(StringUtils.isBlank((String)scmConfig.getConfig().get("repository"))){
            errors.add(new ErrorMessage.Error("scmConfig.repository",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(),
                    "repository is required"));
        }
        return errors;
    }
}
