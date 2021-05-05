package io.jenkins.blueocean.blueocean_github_pipeline;

import com.fasterxml.jackson.databind.JsonMappingException;
import hudson.ProxyConfiguration;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

/**
 * @author Vivek Pandey
 */
class HttpRequest {
    private final String method;
    private final String url;
    private String contentType = "application/json; charset=utf-8";
    private Object body;

    private String authorization;

    private HttpRequest(String method, String url) {
        this.method = method;
        this.url = url;
    }

    public static HttpRequest get(String url){
        return new HttpRequest("GET", url);
    }

    public static HttpRequest head(String url){
        return new HttpRequest("HEAD", url);
    }

    public static HttpRequest post(String url){
        return new HttpRequest("POST", url);
    }

    public static HttpRequest put(String url){
        return new HttpRequest("PUT", url);
    }

    public HttpRequest with(String contentType){
        this.contentType = contentType;
        return this;
    }

    public HttpRequest withAuthorizationToken(String accessToken){
        this.authorization = "token " + accessToken;
        return this;
    }

    public HttpRequest withBody(Object body){
        this.body = body;
        return this;
    }

    public <T> T to(Class<T> type) throws IOException {
        HttpURLConnection connection = connect();
        if (methodNeedsBody()) {
            if (body == null) {
                GithubScm.getMappingObjectWriter().writeValue(connection.getOutputStream(), Collections.emptyMap());
            } else {
                GithubScm.getMappingObjectWriter().writeValue(connection.getOutputStream(), body);
            }
        }


        int status = connection.getResponseCode();
        if (status == 304) {
            return null;
        }
        if (status == 204 && type != null && type.isArray()) {
            return type.cast(Array.newInstance(type.getComponentType(), 0));
        }
        if(status == 401 || status == 403){
            throw new ServiceException.ForbiddenException("Invalid accessToken");
        }
        if(status == 404){
            throw new ServiceException.NotFoundException("Not Found. Remote server sent code " + getErrorResponse(connection));
        }
        if(status > 399) {
            throw new ServiceException.BadRequestException(String.format("%s %s returned error: %s. Error message: %s.", method, url ,status, getErrorResponse(connection)));
        }
        if(!method.equals("HEAD")) {
            try(InputStreamReader r = new InputStreamReader(
                    wrapStream( connection.getInputStream(), connection.getContentEncoding()),
                    StandardCharsets.UTF_8 )){

                String data = IOUtils.toString(r);
                if (type != null ){
                    try {
                        return GithubScm.getMappingObjectReader().forType(type).readValue(data);
                    } catch (JsonMappingException e) {
                        throw new IOException("Failed to deserialize: " + e.getMessage() + "\n" + data, e);
                    }
                }
            }
        }

        return null;
    }

    private String getErrorResponse(HttpURLConnection connection) throws IOException {
        if(connection.getErrorStream() == null){
            return "";
        }
        return IOUtils.toString(wrapStream( connection.getErrorStream(), connection.getContentEncoding()));
    }

    private boolean methodNeedsBody(){
        return (!method.equals("GET") && !method.equals("DELETE"));
    }

    HttpURLConnection connect() throws IOException {
        URL apiUrl = new URL(url);
        ProxyConfiguration proxyConfig = Jenkins.get().proxy;
        Proxy proxy = proxyConfig == null ? Proxy.NO_PROXY : proxyConfig.createProxy(apiUrl.getHost());

        HttpURLConnection connect=(HttpURLConnection) apiUrl.openConnection(proxy);
        if (authorization!=null) {
            connect.setRequestProperty("Authorization", authorization);
        }
        connect.setRequestMethod(method);
        connect.setRequestProperty("Accept-Encoding", "gzip");
        connect.setDoOutput(true);
        connect.setRequestProperty("Content-type", contentType);
        connect.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(10));
        connect.setReadTimeout((int) TimeUnit.SECONDS.toMillis(10));
        connect.connect();
        return connect;
    }

    static InputStream getInputStream(HttpURLConnection connection) throws IOException {
        return wrapStream(connection.getInputStream(), connection.getContentEncoding());
    }

    static InputStream getErrorStream(HttpURLConnection connection) throws IOException {
        return wrapStream(connection.getErrorStream(), connection.getContentEncoding());
    }

    private static InputStream wrapStream(InputStream in, String contentEncoding) throws IOException {
        if (contentEncoding==null || in==null) return in;
        if (contentEncoding.equals("gzip"))    return new GZIPInputStream(in);

        throw new UnsupportedOperationException("Unexpected Content-Encoding: "+contentEncoding);
    }

}

