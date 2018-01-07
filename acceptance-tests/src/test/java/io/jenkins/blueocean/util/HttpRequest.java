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
 * Utility for making requests (usually in the context of JUnit tests).
 * There are two key methods:
 * - One of the "HTTP method" methods (Get, Post, etc) which support uri template syntax,
 * - One of the "as" methods (as(Void.class), as(Map.class), asText(), etc) to execute.
 *
 * Sample usages:
 *
 * Map org = new HttpRequest("http://localhost:8080/jenkins/blue/rest")
 *     .Get("/organizations/{org}")
 *     .urlPart("org", "jenkins")
 *     .as(Map.class);
 *
 * new HttpRequest("...")
 *     .Post("/organizations/jenkins/pipelines")
 *     .auth("username", "password")
 *     .header("X-Foo", "Bar")
 *     .bodyJson(ImmutableMap.of("name", "my-pipeline"))
 *     .as(Void.class);
 *
 * Map error = new HttpRequest("...")
 *      .Post("organizations/jenkins/pipelines")
 *      .status(401)
 *      .as(Map.class)
 *
 * To use it, add "blueocean-rest-impl" as a dep with <type>test-jar</type>
 * then add fluent-hc, handy-uri-templates and stapler as deps.
 *
 * @see <a href="https://github.com/damnhandy/Handy-URI-Templates">https://github.com/damnhandy/Handy-URI-Templates</a>
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

    /**
     * Create an instance that will prepend "baseUrl" to the url specified in Get, Post, etc.
     * @param baseUrl
     */
    public HttpRequest(@Nonnull String baseUrl) {
        Preconditions.checkState(StringUtils.isNotBlank(baseUrl), "baseUrl is required");
        this.baseUrl = baseUrl;
    }

    /**
     * Specify a baseUrl to prepend to the url specified in Get, Post, etc.
     * @param baseUrl url to prepend
     * @return builder
     */
    public HttpRequest baseUrl(@Nonnull String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * Specify a GET request to the specified URL (absolute, or relative if "baseUrl" was specified)
     * @param url
     * @return builder
     */
    public HttpRequest Get(String url) {
        this.method = GET;
        this.requestUrl = url;
        return this;
    }

    /**
     * Specify a POST request to the specified URL (absolute, or relative if "baseUrl" was specified)
     * @param url
     * @return builder
     */
    public HttpRequest Post(String url) {
        this.method = Method.POST;
        this.requestUrl = url;
        return this;
    }

    /**
     * Specify a PUT request to the specified URL (absolute, or relative if "baseUrl" was specified)
     * @param url
     * @return builder
     */
    public HttpRequest Put(String url) {
        this.method = Method.PUT;
        this.requestUrl = url;
        return this;
    }

    /**
     * Specify a PATCH request to the specified URL (absolute, or relative if "baseUrl" was specified)
     * @param url
     * @return builder
     */
    public HttpRequest Patch(String url) {
        this.method = Method.PATCH;
        this.requestUrl = url;
        return this;
    }

    /**
     * Specify a DELETE request to the specified URL (absolute, or relative if "baseUrl" was specified)
     * @param url string which can contain embedded
     * @return builder
     */
    public HttpRequest Delete(String url) {
        this.method = Method.DELETE;
        this.requestUrl = url;
        return this;
    }

    /**
     * Specify the value to be bound to uri template syntax.
     * @param key tempalte variable name
     * @param value value to bind
     * @return builder
     */
    public HttpRequest urlPart(String key, String value) {
        urlParts.put(key, value);
        return this;
    }

    /**
     * Attach a request header.
     * @param name name of header
     * @param value value of header
     * @return builder
     */
    public HttpRequest header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Set a username+password for basic authentication.
     * @param username
     * @param password
     * @return builder
     */
    public HttpRequest auth(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    /**
     * Set contentType of request body.
     * @param contentType
     * @return builder
     */
    public HttpRequest contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Set an object to be JSON-encoded in request body. Sets content type automatically.
     * @param object
     * @return builder
     */
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

    /**
     * Set an expected status code to be sent in response; an exception will be thrown if not matched.
     * If not set, assumes 200.
     * @param statusCode
     * @return builder
     */
    public HttpRequest status(int statusCode) {
        this.expectedStatus = statusCode;
        return this;
    }

    /**
     * Execute the request and transform JSON response body to the specified type.
     * Use as(Void.class) if you wish to ignore the response body.
     * @param clazz
     * @param <T>
     * @return response body as instance of specified class/type
     * @throws IOException
     */
    public <T> T as(Class<T> clazz) throws IOException {
        if (Void.class.equals(clazz)) {
            executeInternal();
            return null;
        }
        return JsonConverter.toJava(asInputStream(), clazz);
    }

    /**
     * Execute the request and return the response body as a string.
     * @return response body as string.
     * @throws IOException
     */
    public String asText() throws IOException {
        return executeInternal().asString();
    }

    /**
     * Execute the request and return the response body as a string of specified charset.
     * @param charset character set of string
     * @return response body as string of specified charset.
     * @throws IOException
     */
    public String asText(String charset) throws IOException {
        return executeInternal().asString(Charset.forName(charset));
    }

    /**
     * Execute the request and return the response as an InputStream
     * @return response body as InputStream
     * @throws IOException
     */
    public InputStream asInputStream() throws IOException {
        return executeInternal().asStream();
    }

    /**
     * Execute the request and return the response as a byte array
     * @return response body as byte array
     * @throws IOException
     */
    public byte[] asBytes() throws IOException {
        return executeInternal().asBytes();
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
