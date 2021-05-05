package io.jenkins.blueocean.blueocean_github_pipeline;

import okhttp3.OkHttpClient;
import hudson.ProxyConfiguration;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.model.Jenkins;
import org.kohsuke.github.AbuseLimitHandler;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;
import org.kohsuke.github.extras.okhttp3.OkHttpConnector;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

class GitHubFactory {

    private static final OkHttpClient baseClient = new OkHttpClient();


    /**
     * Connect to github with the correct limit handlers
     * @param accessToken to authorize
     * @param endpointUri endpoint to connect to
     * @return GitHub
     * @throws IOException if GitHub could not be constructed
     */
    public static GitHub connect(String accessToken, String endpointUri) throws IOException {
        URL apiUrl = new URL(endpointUri);
        ProxyConfiguration proxyConfig = Jenkins.get().proxy;
        Proxy proxy = proxyConfig == null ? Proxy.NO_PROXY : proxyConfig.createProxy(apiUrl.getHost());

        OkHttpClient.Builder builder = baseClient.newBuilder().proxy(proxy);

        return new GitHubBuilder().withOAuthToken(accessToken)
            .withConnector(new OkHttpConnector(builder.build()))
            .withRateLimitHandler(RateLimitHandlerImpl.INSTANCE)
            .withAbuseLimitHandler(AbuseLimitHandlerImpl.INSTANCE)
            .withProxy(proxy)
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
