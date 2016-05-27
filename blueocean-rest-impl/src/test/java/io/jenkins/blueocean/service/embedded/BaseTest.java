package io.jenkins.blueocean.service.embedded;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.blueocean.commons.JsonConverter;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.LogManager;

/**
 * @author Vivek Pandey
 */
public abstract class BaseTest {
    private static  final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);

    @Rule
    public JenkinsRule j = new JenkinsRule();

    protected  String baseUrl;

    protected String getContextPath(){
        return "blue/rest";
    }

    @Before
    public void setup() throws Exception {
        if(System.getProperty("DISABLE_HTTP_HEADER_TRACE") == null) {
            InputStream is = this.getClass().getResourceAsStream("/logging.properties");
            LogManager.getLogManager().readConfiguration(is);
        }
        this.baseUrl = j.jenkins.getRootUrl() + getContextPath();
        Unirest.setObjectMapper(new ObjectMapper() {
            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    if(value.isEmpty()){
                        value = "{}";
                    }

                    T r =  JsonConverter.om.readValue(value, valueType);
                    LOGGER.info("Response:\n"+JsonConverter.om.writeValueAsString(r));
                    return r;
                } catch (IOException e) {
                    LOGGER.info("Failed to parse JSON: "+value+". "+e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    String str = JsonConverter.om.writeValueAsString(value);
                    LOGGER.info("Request:\n"+str);
                    return str;
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

//        HttpClientParams params = new HttpClientParams();
//
//        HttpClient client = new HttpClient();
//        Unirest.setHttpClient();
    }

    protected <T> T  get(String path, Class<T> type){
        return get(path,200, type);
    }

    protected Map<String, Object> get(String path){
        return get(path,200, Map.class);
    }
    @SuppressWarnings("unchecked")
    protected <T> T get(String path, int expectedStatus, Class<T> type){
        assert path.startsWith("/");
        return get(path, expectedStatus, "*/*", type);
    }

    @SuppressWarnings("unchecked")
    protected <T> T get(String path, int expectedStatus, String accept, Class<T> type){
        assert path.startsWith("/");
        try {
            if(HttpResponse.class.isAssignableFrom(type)){
                HttpResponse<String> response = Unirest.get(getBaseUrl(path)).header("Accept", accept)
                    .header("Accept-Encoding","")
                    .asString();
                Assert.assertEquals(expectedStatus, response.getStatus());
                return (T) response;
            }

            HttpResponse<T> response = Unirest.get(getBaseUrl(path)).header("Accept", accept).asObject(type);
            Assert.assertEquals(expectedStatus, response.getStatus());
            return response.getBody();
        } catch (UnirestException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected Map delete(String path){
        assert path.startsWith("/");
        try {
            HttpResponse<Map> response = Unirest.delete(getBaseUrl(path)).asObject(Map.class);
            Assert.assertEquals(200, response.getStatus());
            return response.getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }


    protected Map<String, Object> post(String path, Object body) {
        return post(path,body,200);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> post(String path, Object body, int expectedStatus){
        assert path.startsWith("/");
        try {
            HttpResponse<Map> response = Unirest.post(getBaseUrl(path))
                .header("Content-Type","application/json")
                .body(body).asObject(Map.class);
            Assert.assertEquals(expectedStatus, response.getStatus());
            return response.getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    protected String post(String path, String body, String contentType, int expectedStatus){
        assert path.startsWith("/");
        try {
            HttpResponse<String> response = Unirest.post(getBaseUrl(path))
                .header("Content-Type",contentType)
                .header("Accept-Encoding","")
                .body(body).asObject(String.class);
            Assert.assertEquals(expectedStatus, response.getStatus());
            return response.getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }



    protected Map<String, Object> put(String path, Object body) {
        return put(path, body, 200);
    }
    @SuppressWarnings("unchecked")
    protected Map<String, Object> put(String path, Object body, int expectedStatus){
        assert path.startsWith("/");
        try {
            HttpResponse<Map> response = Unirest.put(getBaseUrl(path))
                .header("Content-Type","application/json")
                .header("Accept","application/json")
                //Unirest by default sets accept-encoding to gzip but stapler is sending malformed gzip value if
                // the response length is small (in this case its 20 chars).
                // Needs investigation in stapler to see whats going on there.
                // For time being gzip compression is disabled
                .header("Accept-Encoding","")
                .body(body).asObject(Map.class);
            Assert.assertEquals(expectedStatus, response.getStatus());
            return response.getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }


    protected String put(String path, String body, String contentType, int expectedStatus){
        assert path.startsWith("/");
        try {
            HttpResponse<String> response = Unirest.put(getBaseUrl(path))
                .header("Content-Type",contentType)
                .body(body).asObject(String.class);
            Assert.assertEquals(expectedStatus, response.getStatus());
            return response.getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    protected Map<String, Object> patch(String path, Object body) {
        return patch(path, body, 200);
    }
    @SuppressWarnings("unchecked")
    protected Map<String, Object> patch(String path, Object body, int expectedStatus){
        assert path.startsWith("/");
        try {
            HttpResponse<Map> response = Unirest.patch(getBaseUrl(path))
                .header("Content-Type","application/json")
                .body(body).asObject(Map.class);
            Assert.assertEquals(expectedStatus, response.getStatus());
            return response.getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }

    }

    protected String patch(String path, String body, String contentType, int expectedStatus){
        assert path.startsWith("/");
        try {
            HttpResponse<String> response = Unirest.patch(getBaseUrl(path))
                .header("Content-Type",contentType)
                .body(body).asObject(String.class);
            Assert.assertEquals(expectedStatus, response.getStatus());
            return response.getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    protected void validateMultiBranchPipeline(WorkflowMultiBranchProject p, Map resp, int numBranches){
        validateMultiBranchPipeline(p, resp, numBranches, -1, -1);
    }
    protected void validateMultiBranchPipeline(WorkflowMultiBranchProject p, Map resp, int numBranches, int numSuccBranches, int numOfFailingBranches){
        Assert.assertEquals("jenkins", resp.get("organization"));
        Assert.assertEquals(p.getName(), resp.get("name"));
        Assert.assertEquals(p.getDisplayName(), resp.get("displayName"));
        Assert.assertNull(resp.get("lastSuccessfulRun"));
        Assert.assertEquals(numBranches, resp.get("totalNumberOfBranches"));
        if(numOfFailingBranches >= 0) {
            Assert.assertEquals(numOfFailingBranches, resp.get("numberOfFailingBranches"));
        }
        if(numSuccBranches >= 0) {
            Assert.assertEquals(numSuccBranches, resp.get("numberOfSuccessfulBranches"));
        }
        Assert.assertEquals(p.getBuildHealth().getScore(), resp.get("weatherScore"));
    }

    protected void validatePipeline(Job p, Map resp){
        Assert.assertEquals("jenkins", resp.get("organization"));
        Assert.assertEquals(p.getName(), resp.get("name"));
        Assert.assertEquals(p.getDisplayName(), resp.get("displayName"));
        Assert.assertEquals(p.getBuildHealth().getScore(), resp.get("weatherScore"));
        if(p.getLastSuccessfulBuild() != null){
            Run b = p.getLastSuccessfulBuild();
            Assert.assertEquals(baseUrl + "/organizations/jenkins/pipelines/" +
                p.getName() + "/runs/" + b.getId(), resp.get("lastSuccessfulRun"));

        }else{
            Assert.assertNull(resp.get("lastSuccessfulRun"));
        }

        if(p.getLastBuild() != null){
            Run r = p.getLastBuild();
            validateRun(r, (Map) resp.get("latestRun"), "FINISHED");
        }else{
            Assert.assertNull(resp.get("latestRun"));
        }
    }

    protected void validateRun(Run r, Map resp){
        validateRun(r,resp, "FINISHED");
    }
    protected void validateRun(Run r, Map resp, String state){
        Assert.assertNotNull(resp);
        Assert.assertEquals(r.getId(), resp.get("id"));
        Assert.assertEquals("jenkins", resp.get("organization"));
        Assert.assertEquals(r.getParent().getName(), resp.get("pipeline"));
        Assert.assertEquals(new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING)
            .format(new Date(r.getStartTimeInMillis())), resp.get("startTime"));
        Assert.assertEquals(r.getResult().toString(), resp.get("result"));
        Assert.assertEquals(state, resp.get("state"));
    }

    protected String getNodeName(FlowNode n){
        return n.getAction(ThreadNameAction.class) != null
            ? n.getAction(ThreadNameAction.class).getThreadName()
            : n.getDisplayName();
    }

    private String getBaseUrl(String path){
        return baseUrl + path;
    }

    public RequestBuilder request() {
        return new RequestBuilder(baseUrl);
    }
    public static class RequestBuilder {
        private String url;
        private String username;
        private String method;
        private String password;
        private Map data;
        private String contentType = "application/json";
        private String baseUrl;
        private int expectedStatus = 200;


        private String getBaseUrl(String path){
            return baseUrl + path;
        }

        public RequestBuilder status(int status) {
            this.expectedStatus = status;
            return this;
        }

        public RequestBuilder(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public RequestBuilder url(String url) {
            this.url = url;
            return this;
        }

        public RequestBuilder auth(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        public RequestBuilder authAlice() {
            this.username = "alice";
            this.password = "alice";
            return this;
        }


        public RequestBuilder data(Map data) {
            this.data = data;
            return this;
        }

        public RequestBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public RequestBuilder put(String url) {
            this.url = url;
            this.method = "PUT";
            return this;
        }


        public RequestBuilder get(String url) {
            this.url = url;
            this.method = "GET";
            return this;
        }

        public RequestBuilder post(String url) {
            this.url = url;
            this.method = "POST";
            return this;
        }


        public RequestBuilder delete(String url) {
            this.url = url;
            this.method = "DELETE";
            return this;
        }

        public <T> T build(Class<T> clzzz) {
            assert url != null;
            assert url.startsWith("/");
            try {
                HttpRequest request;
                switch (method) {
                    case "PUT":
                        request = Unirest.put(getBaseUrl(url));
                        break;
                    case "POST":
                        request = Unirest.post(getBaseUrl(url));
                        break;
                    case "GET":
                        request = Unirest.get(getBaseUrl(url));
                        break;
                    case "DELETE":
                        request = Unirest.delete(getBaseUrl(url));
                        break;
                    default:
                        throw new RuntimeException("No default options");

                }
                request.header("Accept-Encoding","");

                request.header("Content-Type", contentType);
                if(!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)){
                    request.basicAuth(username, password);
                }

                if(request instanceof HttpRequestWithBody && data != null) {
                    ((HttpRequestWithBody)request).body(data);
                }
                HttpResponse<T> response = request.asObject(clzzz);
                Assert.assertEquals(expectedStatus, response.getStatus());
                return response.getBody();
            } catch (UnirestException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
