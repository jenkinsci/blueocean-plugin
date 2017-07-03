package io.jenkins.blueocean.blueocean_github_pipeline;

import io.jenkins.blueocean.commons.ServiceException;
import org.kohsuke.github.AbuseLimitHandler;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;

import java.io.IOException;
import java.net.HttpURLConnection;

class GitHubFactory {

    /**
     * Connect to github with the correct limit handlers
     * @param accessToken to authorize
     * @param endpointUri endpoint to connect to
     * @return GitHub
     * @throws IOException if GitHub could not be constructed
     */
    public static GitHub connect(String accessToken, String endpointUri) throws IOException {
        return new GitHubBuilder().withOAuthToken(accessToken)
            .withRateLimitHandler(RateLimitHandlerImpl.INSTANCE)
            .withAbuseLimitHandler(AbuseLimitHandlerImpl.INSTANCE)
            .withEndpoint(endpointUri).build();
    }

    static class RateLimitHandlerImpl extends RateLimitHandler{
        static final RateLimitHandlerImpl INSTANCE = new RateLimitHandlerImpl();
        @Override
        public void onError(IOException e, HttpURLConnection httpURLConnection) throws IOException {
            throw new ServiceException.BadRequestException("API rate limit reached. Message: " + e.getMessage(), e);
        }
    }

    static class AbuseLimitHandlerImpl extends AbuseLimitHandler {
        static final AbuseLimitHandlerImpl INSTANCE = new AbuseLimitHandlerImpl();
        @Override
        public void onError(IOException e, HttpURLConnection uc) throws IOException {
            throw new ServiceException.BadRequestException("API abuse limit reached. Message: " + e.getMessage(), e);
        }
    }

    private GitHubFactory() {}
}
