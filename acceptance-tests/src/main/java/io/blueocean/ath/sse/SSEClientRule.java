package io.blueocean.ath.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
//import com.mashape.unirest.http.HttpResponse;
//import com.mashape.unirest.http.JsonNode;
//import com.mashape.unirest.http.Unirest;
//import com.mashape.unirest.http.exceptions.UnirestException;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.JenkinsUser;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.SseFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.rules.ExternalResource;
import org.openqa.selenium.support.ui.FluentWait;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;


public class SSEClientRule extends ExternalResource {
    private Logger logger = Logger.getLogger(SSEClientRule.class);

    private Cookie sessionCookie;

    @Override
    protected void before() throws Throwable {
        events = Lists.newCopyOnWriteArrayList();
        connect();
    }

    @Override
    protected void after() {

        if (source != null) {
            source.close();
        }
        clear();

    }

    @Inject
    @BaseUrl
    String baseUrl;

    @Inject
    JenkinsUser admin;

    public SSEClientRule() {
//        mapper = new ObjectMapper();
        System.out.println("new SSEClientRule()!"); // TODO: RM
    }

//    ObjectMapper mapper;

    List<JSONObject> events;

    public List<JSONObject> getEvents() {
        return events;
    }

    public void clear() {
        events.clear();
    }

    private boolean logEvents;

    public boolean isLogEvents() {
        return logEvents;
    }

    public void setLogEvents(boolean logEvents) {
        this.logEvents = logEvents;
    }

    private EventListener listener = inboundEvent -> {
//        System.out.println(">>>>>> SSE inbound " + inboundEvent); // TODO: RM
        JSONObject jenkinsEvent = new JSONObject(inboundEvent.readData());
        if (jenkinsEvent.has("jenkins_event") && jenkinsEvent.getString("jenkins_event").equals("job_run_queue_enter")) {
            if (jenkinsEvent.has("jenkins_object_type") &&
                jenkinsEvent.getString("jenkins_object_type").equals("org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject")) {
            } else if (jenkinsEvent.has("blueocean_job_pipeline_name")) {
                String pipelineName = jenkinsEvent.getString("blueocean_job_pipeline_name");
                logger.info("Build for " + pipelineName + " entered queue");
            }
        }
//        System.out.println(">>>>>> SSE adding jenkinsEvent " + jenkinsEvent); // TODO: RM
        events.add(jenkinsEvent);
        if (logEvents) {
            logger.info("SSE - " + jenkinsEvent.toString());
        }
    };

    EventSource source;

    public void connect() throws UnirestException, InterruptedException {

        SecureRandom rnd = new SecureRandom();
        String clientId = "ath-" + rnd.nextLong();
        HttpResponse<JsonNode> httpResponse = Unirest.get(baseUrl + "/sse-gateway/connect?clientId=" + clientId).basicAuth(admin.username, admin.password).asJson();
        JsonNode body = httpResponse.getBody();
        Client client = ClientBuilder.newBuilder().register(SseFeature.class).build();
        final String httpSessionId = body.getObject().getJSONObject("data").getString("jsessionid");
//        System.out.println("****** gateway connect response body " + body); // TODO: RM
                System.out.println("httpSessionId: " + httpSessionId); // TODO: RM
//        WebTarget target = client.target(baseUrl + "/sse-gateway/listen/" + clientId + ";jsessionid=" + httpSessionId);
//        Cookie sessionCookie = new Cookie("JSESSIONID",httpSessionId);

        checkResponseForCookie(httpResponse);

        WebTarget target = client.target(baseUrl + "/sse-gateway/listen/" + clientId);

        if (sessionCookie != null) {
            target = new CookieAddedWebTarget(target,sessionCookie);
        } else {
            // TODO: fix it so this doesn't happen, or create a new valid cookie somehow!
            throw new RuntimeException("Reusing session " + httpSessionId + " but this instance has never seen the cookie!");
        }

        // TODO: put sessionid on url for old jenkins, but cookie for modern jenkins?





        source = EventSource.target(target).build();
        source.register(listener);
        source.open();

        JSONObject req = new JSONObject()
            .put("dispatcherId", clientId)
            .put("subscribe", new JSONArray(ImmutableList.of(
                new JSONObject().put("jenkins_org", "jenkins")
                                .put("jenkins_channel", "job"))))
            .put("unsubscribe", new JSONArray());

        HttpResponse<JsonNode> result = Unirest.post(baseUrl + "/sse-gateway/configure?batchId=1")
                                               .basicAuth(admin.username, admin.password)
                                               .body(req).asJson();

        logger.info("SSE Connected " + clientId);
    }

    /**
     * Checks the headers for the session cookie and extracts it when received, so we can use it on subsequent
     * tests within the same session.
     */
    private void checkResponseForCookie(HttpResponse<?> httpResponse) {
        List<String> cookies = httpResponse.getHeaders().get("Set-Cookie");

        if (cookies != null) {
            for (String rawCookie : cookies) {
                if (rawCookie.toUpperCase().contains("JSESSIONID")) {
                    System.out.println("Session cookie found " + rawCookie); // TODO: RM
                    this.sessionCookie = Cookie.valueOf(rawCookie);
                    break;
                }
            }
        }
    }

    public void untilEvent(Predicate<JSONObject> isEvent) {
        new FluentWait<List<JSONObject>>(getEvents())
            .pollingEvery(1000, TimeUnit.MILLISECONDS)
            .withTimeout(20, TimeUnit.SECONDS)
            .ignoring(NoSuchElementException.class)
            .until((Predicate<List<JSONObject>>) events -> Iterables.any(events, isEvent));
    }

    public void untilEvents(Predicate<List<JSONObject>> isEvents) {
        new FluentWait<>(getEvents())
            .pollingEvery(1000, TimeUnit.MILLISECONDS)
            .withTimeout(120, TimeUnit.SECONDS)
            .ignoring(NoSuchElementException.class)
            .until((Predicate<List<JSONObject>>) a -> {
                return isEvents.apply(a);
            });
    }
}

/**
 * Because neither WebTarget *or* EventSource lets you set cookies, and adding jsessionid to the URL doesn't work in
 * Jenkins 1.150.1. If you know a less-awful way to do this, please do.
 * <p>
 * Inspired by / ripped from https://stackoverflow.com/questions/33626412/server-sent-event-client-with-additional-cookie
 */
class CookieAddedWebTarget implements WebTarget {

    private WebTarget delegate;
    private Cookie cookie;

    public CookieAddedWebTarget(WebTarget delegate, Cookie cookie) {
        this.delegate = delegate;
        this.cookie = cookie;
    }

    // Inject that cookie whenever someone requests a Builder (which EventSource does):
    public Invocation.Builder request() {
        return delegate.request().cookie(cookie);
    }

    public Invocation.Builder request(String... paramArrayOfString) {
        return delegate.request(paramArrayOfString).cookie(cookie);
    }

    public Invocation.Builder request(MediaType... paramArrayOfMediaType) {
        return delegate.request(paramArrayOfMediaType).cookie(cookie);
    }

    //All other methods from WebTarget are delegated as-is:

    @Override
    public URI getUri() {
        return delegate.getUri();
    }

    @Override
    public UriBuilder getUriBuilder() {
        return delegate.getUriBuilder();
    }

    @Override
    public WebTarget path(String s) {
        return delegate.path(s);
    }

    @Override
    public WebTarget resolveTemplate(String s, Object o) {
        return delegate.resolveTemplate(s, o);
    }

    @Override
    public WebTarget resolveTemplate(String s, Object o, boolean b) {
        return delegate.resolveTemplate(s, o, b); // That aughtta hold those SOBs </brockman>
    }

    @Override
    public WebTarget resolveTemplateFromEncoded(String s, Object o) {
        return delegate.resolveTemplateFromEncoded(s, o);
    }

    @Override
    public WebTarget resolveTemplates(Map<String, Object> map) {
        return delegate.resolveTemplates(map);
    }

    @Override
    public WebTarget resolveTemplates(Map<String, Object> map, boolean b) {
        return delegate.resolveTemplates(map, b);
    }

    @Override
    public WebTarget resolveTemplatesFromEncoded(Map<String, Object> map) {
        return delegate.resolveTemplatesFromEncoded(map);
    }

    @Override
    public WebTarget matrixParam(String s, Object... objects) {
        return delegate.matrixParam(s, objects);
    }

    @Override
    public WebTarget queryParam(String s, Object... objects) {
        return delegate.queryParam(s, objects);
    }

    @Override
    public Configuration getConfiguration() {
        return delegate.getConfiguration();
    }

    @Override
    public WebTarget property(String s, Object o) {
        return delegate.property(s, o);
    }

    @Override
    public WebTarget register(Class<?> aClass) {
        return delegate.register(aClass);
    }

    @Override
    public WebTarget register(Class<?> aClass, int i) {
        return delegate.register(aClass, i);
    }

    @Override
    public WebTarget register(Class<?> aClass, Class<?>... classes) {
        return delegate.register(aClass, classes);
    }

    @Override
    public WebTarget register(Class<?> aClass, Map<Class<?>, Integer> map) {
        return delegate.register(aClass, map);
    }

    @Override
    public WebTarget register(Object o) {
        return delegate.register(o);
    }

    @Override
    public WebTarget register(Object o, int i) {
        return delegate.register(o, i);
    }

    @Override
    public WebTarget register(Object o, Class<?>... classes) {
        return delegate.register(o, classes);
    }

    @Override
    public WebTarget register(Object o, Map<Class<?>, Integer> map) {
        return delegate.register(o, map);
    }

}
