package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.ProxyConfiguration;
import hudson.util.Secret;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Vivek Pandey
 */
@Restricted(NoExternalUse.class)
public class HttpRequest {
    private final HttpClient client;
    private final String authorizationHeader;

    private HttpRequest(@NonNull String apiUrl, @Nullable StandardUsernamePasswordCredentials credentials, @Nullable String authHeader) {
        this.client = getHttpClient(apiUrl);
        if(StringUtils.isBlank(authHeader) && credentials != null) {
            this.authorizationHeader = String.format("Basic %s",
                    Base64.getEncoder().encodeToString(String.format("%s:%s", credentials.getUsername(),
                            Secret.toString(credentials.getPassword())).getBytes(StandardCharsets.UTF_8)));
        }else{
            this.authorizationHeader = authHeader;
        }

    }

    public HttpResponse head(String url) {
        try {
            return new HttpResponse(client.execute(setAuthorizationHeader(new HttpHead(url))));
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    public HttpResponse get(String url) {
        try {
            return new HttpResponse(client.execute(setAuthorizationHeader(new HttpGet(url))));
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    public HttpResponse put(String url, HttpEntity body) {
        try {
            HttpPut httpPut = new HttpPut(url);
            httpPut.setEntity(body);
            return new HttpResponse(client.execute(setAuthorizationHeader(httpPut)));
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    public HttpResponse post(String url, HttpEntity body) {
        try {
            HttpPost post = new HttpPost(url);
            post.setEntity(body);
            return new HttpResponse(client.execute(setAuthorizationHeader(post)));
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    /**
     * Converts thrown exception during BB HTTP call in to JSON serializable {@link ServiceException}
     *
     * @param e exception
     * @return {@link ServiceException} instance
     */
    private ServiceException handleException(Exception e){
        if(e instanceof HttpResponseException){
            return new ServiceException(((HttpResponseException) e).getStatusCode(), e.getMessage(), e);
        }
        return new ServiceException.UnexpectedErrorException(e.getMessage(), e);
    }


    private  HttpClient getHttpClient(@NonNull String apiUrl) {
        HttpClientBuilder clientBuilder = HttpClientBuilder.create().disableAutomaticRetries()
                .disableRedirectHandling();
        setClientProxyParams(apiUrl, clientBuilder);
        return clientBuilder.build();
    }

    private void setClientProxyParams(String apiUrl, HttpClientBuilder clientBuilder) {
        try {
            URL url = new URL(apiUrl);
            ProxyConfiguration proxyConfig = Jenkins.getInstance().proxy;
            Proxy proxy = proxyConfig != null ? proxyConfig.createProxy(url.getHost()) : Proxy.NO_PROXY;
            if (!proxy.equals(Proxy.NO_PROXY) && proxyConfig != null) {
                clientBuilder.setProxy(new HttpHost(proxyConfig.name, proxyConfig.port));
            }
        } catch (MalformedURLException e) {
            throw new ServiceException.UnexpectedErrorException("Invalid apiUrl: "+apiUrl, e);
        }
    }

    private HttpUriRequest setAuthorizationHeader(HttpUriRequest request){
        if(StringUtils.isNotBlank(authorizationHeader)) {
            request.addHeader("Authorization", authorizationHeader);
        }
        return request;
    }

    public static class HttpRequestBuilder {
        private final String url;
        private String authHeader;
        private StandardUsernamePasswordCredentials credentials;

        public HttpRequestBuilder(@NonNull String apiUrl) {
            this.url = apiUrl;
        }

        public HttpRequestBuilder credentials(StandardUsernamePasswordCredentials credentials){
            this.credentials = credentials;
            return this;
        }

        public HttpRequestBuilder authenticationHeader(String authenticationHeader) {
            this.authHeader = authenticationHeader;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(url, credentials, authHeader);
        }
    }

}
