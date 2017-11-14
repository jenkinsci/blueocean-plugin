package io.jenkins.blueocean.util;

import com.damnhandy.uri.template.UriTemplate;
import com.google.common.base.Preconditions;
import io.jenkins.blueocean.commons.JsonConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.ContentResponseHandler;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static io.jenkins.blueocean.util.HttpRequest.Method.GET;

/**
 * TODO: needs javadoc once API is final
 * @author cliffmeyers
 */
public class HttpRequest {

    private static final Logger logger = Logger.getLogger(HttpRequest.class);
    private static final String JSON = "application/json";

    enum Method { GET, POST, PUT, PATCH, DELETE };

    final private Map<String, String> headers = new HashMap<>();
    final private Map<String, Object> urlParts = new HashMap<>();
    private Method method = GET;
    private int expectedStatus = 200;
    private String baseUrl = "";
    private String requestUrl;
    private String contentType;
    private String requestBody;
    private String username;
    private String password;


    public HttpRequest() {}

    public HttpRequest(@Nonnull String baseUrl) {
        Preconditions.checkState(StringUtils.isNotBlank(baseUrl), "baseUrl is required");
        this.baseUrl = baseUrl;
    }

    public HttpRequest baseUrl(@Nonnull String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public HttpRequest Get(String url) {
        this.method = GET;
        this.requestUrl = url;
        return this;
    }

    public HttpRequest Post(String url) {
        this.method = Method.POST;
        this.requestUrl = url;
        return this;
    }

    public HttpRequest Put(String url) {
        this.method = Method.PUT;
        this.requestUrl = url;
        return this;
    }

    public HttpRequest Patch(String url) {
        this.method = Method.PATCH;
        this.requestUrl = url;
        return this;
    }

    public HttpRequest Delete(String url) {
        this.method = Method.DELETE;
        this.requestUrl = url;
        return this;
    }

    public HttpRequest urlPart(String key, String value) {
        urlParts.put(key, value);
        return this;
    }

    public HttpRequest header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public HttpRequest auth(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    public HttpRequest contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public HttpRequest bodyJson(Object object) {
        if (object != null) {
            contentType(JSON);
            requestBody = JsonConverter.toJson(object);
        } else {
            contentType(null);
            requestBody = null;
        }
        return this;
    }

    public HttpRequest status(int statusCode) {
        this.expectedStatus = statusCode;
        return this;
    }

    public <T> T asObject(Class<T> clazz) throws IOException {
        return JsonConverter.toJava(asInputStream(), clazz);
    }

    public String asText() throws IOException {
        return executeInternal().asString();
    }

    public String asText(String charset) throws IOException {
        return executeInternal().asString(Charset.forName(charset));
    }

    public InputStream asInputStream() throws IOException {
        return executeInternal().asStream();
    }

    public byte[] asBytes() throws IOException {
        return executeInternal().asBytes();
    }

    public void execute() throws IOException {
        executeInternal();
    }

    private Content executeInternal() throws IOException {
        String uriPath = urlParts.size() > 0 ?
            UriTemplate.fromTemplate(requestUrl).expand(urlParts) :
            requestUrl;

        URIBuilder uri;
        String fullUrl;

        try {
            uri = new URIBuilder(baseUrl + uriPath);
            fullUrl = uri.toString();
        } catch (URISyntaxException ex ) {
            throw new RuntimeException("could not parse request URL: " + baseUrl + requestUrl, ex);
        }

        logger.info("request url: " + fullUrl);

        Request request;

        switch (method) {
            case GET:
                request = Request.Get(fullUrl); break;
            case POST:
                request = Request.Post(fullUrl); break;
            case PUT:
                request = Request.Put(fullUrl); break;
            case PATCH:
                request = Request.Patch(fullUrl); break;
            case DELETE:
                request = Request.Delete(fullUrl); break;
            default:
                throw new RuntimeException("Invalid method: " + method);
        }

        headers.forEach(request::setHeader);

        if (requestBody != null) {
            request.bodyString(requestBody, ContentType.parse(contentType));
        }

        Executor exec = Executor.newInstance();

        // use 'Authorization: Basic' for username/password
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            String authHost = uri.getPort() != -1 ?
                String.format("%s:%s", uri.getHost(), uri.getPort()) :
                uri.getHost();

            exec.authPreemptive(authHost)
                .auth(username, password);
        }

        try {
            Response response = exec.execute(request);
            HttpResponse httpResponse = response.returnResponse();
            int returnedStatusCode = httpResponse.getStatusLine().getStatusCode();

            if (expectedStatus != returnedStatusCode) {
                throw new RuntimeException(String.format("Status code %s did not match expected %s", returnedStatusCode, expectedStatus));
            }

            // manually build content to avoid 'already consumed' exception from response.returnContent()
            return new ContentResponseHandler()
                .handleEntity(httpResponse.getEntity());
        }
        catch (HttpResponseException ex) {
            throw new RuntimeException("Unexpected error during request", ex);
        }
    }
}
