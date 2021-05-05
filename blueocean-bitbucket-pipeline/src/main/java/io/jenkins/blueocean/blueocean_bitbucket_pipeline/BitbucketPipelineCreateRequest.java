package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceBuilder;
import com.cloudbees.jenkins.plugins.bitbucket.BranchDiscoveryTrait;
import com.cloudbees.jenkins.plugins.bitbucket.ForkPullRequestDiscoveryTrait;
import com.cloudbees.jenkins.plugins.bitbucket.OriginPullRequestDiscoveryTrait;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import jenkins.plugins.git.traits.CleanAfterCheckoutTrait;
import jenkins.plugins.git.traits.CleanBeforeCheckoutTrait;
import jenkins.plugins.git.traits.LocalBranchTrait;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Vivek Pandey
 */
public class BitbucketPipelineCreateRequest extends AbstractMultiBranchCreateRequest {
    private static final Logger logger = LoggerFactory.getLogger(BitbucketPipelineCreateRequest.class);

    @DataBoundConstructor
    public BitbucketPipelineCreateRequest(String name, BlueScmConfig scmConfig) {
        super(name, scmConfig);
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    @Override
    protected SCMSource createSource(@Nonnull MultiBranchProject project, @Nonnull BlueScmConfig scmConfig) {
        /* scmConfig.uri presence is already validated in {@link #validate} but lets check just in case */
        if(StringUtils.isBlank(scmConfig.getUri())){
            throw new ServiceException.BadRequestException("scmConfig.uri must be present");
        }

        Set<ChangeRequestCheckoutStrategy> strategies = new HashSet<>();
        strategies.add(ChangeRequestCheckoutStrategy.MERGE);

        BitbucketSCMSource bitbucketSCMSource = new BitbucketSCMSourceBuilder(null, scmConfig.getUri(), computeCredentialId(scmConfig),
                (String)scmConfig.getConfig().get("repoOwner"),
                (String)scmConfig.getConfig().get("repository"))
                .withTrait(new BranchDiscoveryTrait(true, true)) //take all branches
                .withTrait(new ForkPullRequestDiscoveryTrait(strategies, new ForkPullRequestDiscoveryTrait.TrustTeamForks()))
                .withTrait(new OriginPullRequestDiscoveryTrait(strategies))
                .withTrait(new CleanBeforeCheckoutTrait())
                .withTrait(new CleanAfterCheckoutTrait())
                .withTrait(new LocalBranchTrait())
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
    protected AbstractScmSourceEvent getScmSourceEvent(@Nonnull final MultiBranchProject project, @Nonnull SCMSource source) {
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
                    return !criteria.isJenkinsfileFound();

                }
            }, TaskListener.NULL);
        } catch (IOException | InterruptedException e) {
            logger.warn("Error detecting Jenkinsfile: "+e.getMessage(), e);
        }

        return criteria.isJenkinsfileFound();
    }

    @Override
    protected List<ErrorMessage.Error> validate(String name, BlueScmConfig scmConfig) {
        List<ErrorMessage.Error> errors = Lists.newArrayList();
        StandardUsernamePasswordCredentials credentials = null;
        String credentialId = computeCredentialId(scmConfig);
        if(credentialId != null){
            credentials = CredentialsUtils.findCredential(credentialId, StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());
        }
        if (credentials == null) {
            errors.add(new ErrorMessage.Error("scmConfig.credentialId",
                ErrorMessage.Error.ErrorCodes.NOT_FOUND.toString(),
                "No Credentials instance found for credentialId: " + credentialId));
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

    @Override
    protected String computeCredentialId(BlueScmConfig scmConfig) {
        return BitbucketCredentialUtils.computeCredentialId(scmConfig.getCredentialId(), scmConfig.getId(), scmConfig.getUri());
    }
}
