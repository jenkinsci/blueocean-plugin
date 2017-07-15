package io.jenkins.blueocean.blueocean_github_pipeline;

import com.fasterxml.jackson.databind.JsonMappingException;
import io.jenkins.blueocean.commons.ServiceException;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.HttpConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
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

    public HttpRequest withAuthorization(String authorization){
        this.authorization = authorization;
        return this;
    }

    public HttpRequest withBody(Object body){
        this.body = body;
        return this;
    }

    public <T> T to(Class<T> type) throws IOException {
        HttpURLConnection connection = connect();
        if (methodNeedsBody()) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-type", contentType);
            if (body == null) {
                GithubScm.om.writeValue(connection.getOutputStream(), Collections.emptyMap());
            } else {
                GithubScm.om.writeValue(connection.getOutputStream(), body);
            }
        }
        InputStreamReader r=null;
        try {
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
                r = new InputStreamReader(wrapStream(connection.getInputStream(), connection.getContentEncoding()), "UTF-8");
                String data = IOUtils.toString(r);
                if (type != null) {
                    try {
                        return GithubScm.om.readValue(data, type);
                    } catch (JsonMappingException e) {
                        throw new IOException("Failed to deserialize " + data, e);
                    }
                }
            }
        }finally {
            IOUtils.closeQuietly(r);
        }
        return null;
    }

    private String getErrorResponse(HttpURLConnection connection) throws IOException {
        if(connection.getErrorStream() == null){
            return "";
        }
        return IOUtils.toString(wrapStream(connection.getErrorStream(), connection.getContentEncoding()));
    }

    private boolean methodNeedsBody(){
        return (!method.equals("GET") && !method.equals("DELETE"));
    }

    private HttpURLConnection connect() throws IOException {
        HttpURLConnection connect = HttpConnector.DEFAULT.connect(new URL(url));
        if (authorization!=null) {
            connect.setRequestProperty("Authorization", authorization);
        }
        connect.setRequestMethod(method);
        connect.setRequestProperty("Accept-Encoding", "gzip");
        connect.setDoOutput(true);
        connect.setRequestProperty("Content-type", contentType);
        return connect;
    }

    private InputStream wrapStream(InputStream in, String contentEncoding) throws IOException {
        if (contentEncoding==null || in==null) return in;
        if (contentEncoding.equals("gzip"))    return new GZIPInputStream(in);

        throw new UnsupportedOperationException("Unexpected Content-Encoding: "+contentEncoding);
    }

}

