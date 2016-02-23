package io.jenkins.blueocean.auth.github;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import hudson.Extension;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.security.AuthenticationProvider;
import io.jenkins.blueocean.security.UserPrototype;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.GitHubTokenResponse;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Ivan Meredith
 */
@Extension
public class GithubAuthenticationProvider extends AuthenticationProvider<GithubCredentials> {

    private GithubConfig githubConfig;

    public GithubAuthenticationProvider(){}

    @Inject
    public GithubAuthenticationProvider(GithubConfig githubConfig){
        this.githubConfig = githubConfig;
    }

    @Override
    public GithubCredentials getCredentials(HttpServletRequest req) {
        return new GithubCredentials(req.getParameter("login"), req.getParameter("code"));
    }

    @Override
    @Nonnull
    public UserPrototype validate(GithubCredentials loginDetails) {
        return fetchUser(getAccessToken(loginDetails));
    }

    private String getAccessToken(GithubCredentials loginDetails) {
        OAuthClientRequest clientRequest;
        try {
            OAuthClientRequest.OAuthRequestBuilder builder = OAuthClientRequest
                .tokenProvider(OAuthProviderType.GITHUB)
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setClientId(githubConfig.getGithubClientId())
                .setClientSecret(githubConfig.getGithubClientSecret())
                .setCode(loginDetails.accessToken);

            clientRequest = builder.buildQueryMessage();

        } catch (OAuthSystemException e) {
            throw Throwables.propagate(e);
        }

        OAuthClient client = new OAuthClient(new URLConnectionClient());
        GitHubTokenResponse clientResponse;
        try {
            clientResponse = client.accessToken(clientRequest, GitHubTokenResponse.class);
        } catch (OAuthSystemException e) {
            throw new ServiceException.UnexpectedErrorExpcetion("Exception", e);
        } catch (OAuthProblemException e){
            throw new ServiceException.UnexpectedErrorExpcetion("Exception", e);
        }
        return clientResponse.getAccessToken();
    }

    private GHMyself getGhMyself(String token) throws ServiceException {
        GitHub gitHub;
        try {
            gitHub = new GitHubBuilder()
                .withOAuthToken(token)
                .withRateLimitHandler(RateLimitHandler.FAIL)
                .build();
            return gitHub.getMyself();
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorExpcetion("github", e);
        }
    }

    private UserPrototype fetchUser(String accessToken) {
        GHMyself myself = getGhMyself(accessToken);
        try {
            return new UserPrototype(Objects.firstNonNull(myself.getName(), myself.getLogin()), myself.getEmail());
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorExpcetion("Github user fetch");
        }
    }

    @Override
    public String getType() {
        return "github";
    }

    @Override
    public boolean allowSignup() {
        return true;
    }

    @Override
    public String getLoginUrl() {
        return String.format("https://github.com/login/oauth/authorize?client_id=%s", githubConfig.getGithubClientId());
    }
}
