package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import io.jenkins.blueocean.scm.api.AbstractMultiBranchCreateRequest;
import io.jenkins.blueocean.scm.api.AbstractScmSourceEvent;
import jenkins.branch.MultiBranchProject;
import jenkins.plugins.git.traits.CleanAfterCheckoutTrait;
import jenkins.plugins.git.traits.CleanBeforeCheckoutTrait;
import jenkins.plugins.git.traits.LocalBranchTrait;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github_branch_source.BranchDiscoveryTrait;
import org.jenkinsci.plugins.github_branch_source.Endpoint;
import org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait;
import org.jenkinsci.plugins.github_branch_source.GitHubConfiguration;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceBuilder;
import org.jenkinsci.plugins.github_branch_source.OriginPullRequestDiscoveryTrait;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Vivek Pandey
 */
public class GithubPipelineCreateRequest extends AbstractMultiBranchCreateRequest {
    @DataBoundConstructor
    public GithubPipelineCreateRequest(String name, BlueScmConfig scmConfig) {
        super(name, scmConfig);
    }

    @Override
    protected SCMSource createSource(@Nonnull MultiBranchProject project, @Nonnull BlueScmConfig scmConfig) {
        // Update endpoint only if its GitHub Enterprise
        if(scmConfig.getId().equals(GithubEnterpriseScm.ID)) {
            updateEndpoints(scmConfig.getUri());
        }

        Set<ChangeRequestCheckoutStrategy> strategies = new HashSet<>();
        strategies.add(ChangeRequestCheckoutStrategy.MERGE);

        return new GitHubSCMSourceBuilder(null, scmConfig.getUri(), computeCredentialId(scmConfig),
                (String)scmConfig.getConfig().get("repoOwner"),
                (String)scmConfig.getConfig().get("repository"))
                .withTrait(new BranchDiscoveryTrait(true, true)) //take all branches
                .withTrait(new ForkPullRequestDiscoveryTrait(strategies, new ForkPullRequestDiscoveryTrait.TrustPermission()))
                .withTrait(new OriginPullRequestDiscoveryTrait(strategies))
                .withTrait(new CleanBeforeCheckoutTrait())
                .withTrait(new CleanAfterCheckoutTrait())
                .withTrait(new LocalBranchTrait())
                .build();
    }

    @Nullable
    @Override
    protected AbstractScmSourceEvent getScmSourceEvent(@Nonnull final MultiBranchProject project, @Nonnull SCMSource source) {
        if(source instanceof GitHubSCMSource) {
            return new AbstractScmSourceEvent(((GitHubSCMSource)source).getRepository(),
                    ((GitHubSCMSource)source).getApiUri()) {
                @Override
                public boolean isMatch(@NonNull SCMSource source) {
                    SCMSourceOwner sourceOwner = source.getOwner();
                    return ((GitHubSCMSource)source).getRepository().equals(getSourceName()) && sourceOwner != null
                            && sourceOwner.getFullName().equals(project.getFullName());
                }
            };
        }
        return null;
    }

    @Override
    protected List<ErrorMessage.Error> validate(String name, BlueScmConfig scmConfig) {
        List<ErrorMessage.Error> errors = new ArrayList();
        StandardUsernamePasswordCredentials credentials = null;
        String credentialId = computeCredentialIdWithGithubDefault(scmConfig);
        if(StringUtils.isBlank(scmConfig.getUri())){
            errors.add(new ErrorMessage.Error("scmConfig.uri",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(),
                    "uri is required"));
        }

        if(credentialId != null){
            credentials = CredentialsUtils.findCredential(credentialId, StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());
        }
        if (credentials == null) {
            errors.add(new ErrorMessage.Error("scmConfig.credentialId",
                    ErrorMessage.Error.ErrorCodes.NOT_FOUND.toString(),
                    "No Credentials instance found for credentialId: " + credentialId));
        }else if(StringUtils.isNotBlank(scmConfig.getUri())){
            String accessToken = credentials.getPassword().getPlainText();
            try {
                validateGithubAccessToken(accessToken, scmConfig.getUri());
            } catch (IOException e) {
                throw new ServiceException.UnexpectedErrorException(e.getMessage());
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

    @Override
    protected String computeCredentialId(BlueScmConfig scmConfig) {
        return GithubCredentialUtils.computeCredentialId(scmConfig.getCredentialId(), scmConfig.getId(), scmConfig.getUri());
    }

    // NOTE: if ScmConfig.id is omitted, we assume "github" for backwards compat with old Editor
    private String computeCredentialIdWithGithubDefault(BlueScmConfig blueScmConfig) {
        if (StringUtils.isBlank(blueScmConfig.getId())) {
            return GithubScm.ID;
        }

        return computeCredentialId(blueScmConfig);
    }

    private void updateEndpoints(String apiUrl) {
        GitHubConfiguration config = GitHubConfiguration.get();
        synchronized (config) {
            final String finalApiUrl = apiUrl;
            Optional<Endpoint> optionalEndpoint = config.getEndpoints()
                .stream()
                .filter( input -> input != null && input.getApiUri().equals( finalApiUrl))
                .findFirst();
            Endpoint endpoint = optionalEndpoint.isPresent()? optionalEndpoint.get():null;

            if (endpoint == null) {
                config.setEndpoints(Collections.singletonList(new Endpoint( apiUrl, apiUrl)));
                config.save();
            }
        }
    }

    private static void validateGithubAccessToken(String accessToken, String apiUrl) throws IOException {
        try {
            String cleanApiUrl = apiUrl.endsWith("/") ?
                    apiUrl.substring(0, apiUrl.length() - 1) : apiUrl;
            HttpURLConnection connection =  GithubScm.connect(cleanApiUrl + "/user", accessToken);
            GithubScm.validateAccessTokenScopes(connection);
        } catch (Exception e) {
            if(e instanceof ServiceException){
                throw e;
            }
            throw new ServiceException.UnexpectedErrorException("Failure validating github access token: "+e.getMessage(), e);
        }
    }
}
