package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceBuilder;
import com.cloudbees.jenkins.plugins.bitbucket.BranchDiscoveryTrait;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import io.jenkins.blueocean.scm.api.AbstractMultiBranchCreateRequest;
import io.jenkins.blueocean.scm.api.AbstractScmSourceEvent;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceOwner;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class BitbucketPipelineCreateRequest extends AbstractMultiBranchCreateRequest {
    private static final Logger logger = LoggerFactory.getLogger(BitbucketPipelineCreateRequest.class);

    @DataBoundConstructor
    public BitbucketPipelineCreateRequest(String name, BlueScmConfig scmConfig) {
        super(name, scmConfig);
    }

    @Override
    protected SCMSource createSource(@Nonnull MultiBranchProject project, @Nonnull BlueScmConfig scmConfig) {
        /* scmConfig.uri presence is already validated in {@link #validate} but lets check just in case */
        if(StringUtils.isBlank(scmConfig.getUri())){
            throw new ServiceException.BadRequestException("scmConfig.uri must be present");
        }
        BitbucketSCMSource bitbucketSCMSource = new BitbucketSCMSourceBuilder(null, scmConfig.getUri(), scmConfig.getCredentialId(),
                (String)scmConfig.getConfig().get("repoOwner"),
                (String)scmConfig.getConfig().get("repository"))
                .withTrait(new BranchDiscoveryTrait(3)) //take all branches
                .build();

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

    @Nullable
    @Override
    protected AbstractScmSourceEvent getScmSourceEvent(final MultiBranchProject project, SCMSource source) {
        if(source instanceof BitbucketSCMSource) {
            return new AbstractScmSourceEvent(((BitbucketSCMSource)source).getRepository(),
                    ((BitbucketSCMSource)source).getServerUrl()) {
                @Override
                public boolean isMatch(@NonNull SCMSource source) {
                    SCMSourceOwner sourceOwner = source.getOwner();
                    return ((BitbucketSCMSource)source).getRepository().equals(getSourceName()) && sourceOwner != null
                            && sourceOwner.getFullName().equals(project.getFullName());
                }
            };
        }
        return null;
    }

    @Override
    protected boolean repoHasJenkinsFile(@Nonnull SCMSource scmSource) {
        final JenkinsfileCriteria criteria = new JenkinsfileCriteria();
        try {
            scmSource.fetch(criteria, new SCMHeadObserver() {
                @Override
                public void observe(@Nonnull SCMHead head, @Nonnull SCMRevision revision) throws IOException, InterruptedException {
                    //do nothing
                }

                @Override
                public boolean isObserving() {
                    //if jenkinsfile is found stop observing
                    return !criteria.isJekinsfileFound();

                }
            }, TaskListener.NULL);
        } catch (IOException | InterruptedException e) {
            logger.warn("Error detecting Jenkinsfile: "+e.getMessage(), e);
        }

        return criteria.isJekinsfileFound();
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

        if(StringUtils.isBlank(scmConfig.getUri())){
            errors.add(new ErrorMessage.Error("scmConfig.uri",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(),
                    "uri is required"));
        }

        return errors;
    }
}
