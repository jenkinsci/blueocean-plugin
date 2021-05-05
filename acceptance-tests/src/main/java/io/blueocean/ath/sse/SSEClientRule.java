package io.blueocean.ath.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.JenkinsUser;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.SseFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.rules.ExternalResource;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


public class SSEClientRule extends ExternalResource {
    private Logger logger = LoggerFactory.getLogger(SSEClientRule.class);

    @Override
    protected void before() throws Throwable {
        events = new CopyOnWriteArrayList<>();
        connect();
    }

    @Override
    protected void after() {
        clear();
    }

    @Inject
    @BaseUrl
    String baseUrl;

    @Inject
    JenkinsUser admin;

    public SSEClientRule() {
        mapper = new ObjectMapper();
    }

    ObjectMapper mapper;

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
        JSONObject jenkinsEvent = new JSONObject(inboundEvent.readData());
        if (jenkinsEvent.has("jenkins_event") && jenkinsEvent.getString("jenkins_event").equals("job_run_queue_enter")) {
            if (jenkinsEvent.has("jenkins_object_type") &&
                jenkinsEvent.getString("jenkins_object_type").equals("org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject")) {
            } else if (jenkinsEvent.has("blueocean_job_pipeline_name")) {
                String pipelineName = jenkinsEvent.getString("blueocean_job_pipeline_name");
                logger.info("Build for {} entered queue", pipelineName);
            }
        }
        events.add(jenkinsEvent);
        if (logEvents) {
            logger.info("SSE - {}", jenkinsEvent.toString());
        }
    };

    private EventSource source;
    private Cookie sessionCookie;

    public void connect() {

        SecureRandom rnd = new SecureRandom();
        String clientId = "ath-" + rnd.nextLong();

        Client restClient = ClientBuilder.newClient().register(HttpAuthenticationFeature.basic(admin.username, admin.password));
        Response connectResponse = restClient.target(baseUrl + "/sse-gateway/connect?clientId=" + clientId).request().get();

        checkResponseForCookie(connectResponse);

        Client sseClient = ClientBuilder.newBuilder().register(SseFeature.class).build();
        WebTarget target = sseClient.target(baseUrl + "/sse-gateway/listen/" + clientId);

        if (sessionCookie != null) {
            target = new CookieAddedWebTarget(target, sessionCookie);
        }

        source = EventSource.target(target).build();
        source.register(listener);
        source.open();

        JSONObject req = new JSONObject()
            .put("dispatcherId", clientId)
            .put("subscribe", new JSONArray(ImmutableList.of(
                new JSONObject().put("jenkins_org", "jenkins")
                                .put("jenkins_channel", "job"))))
            .put("unsubscribe", new JSONArray());

        Response configureResponse = restClient
            .target(baseUrl + "/sse-gateway/configure?batchId=1")
            .request()
            .cookie(sessionCookie)
            .buildPost(Entity.json(req.toString()))
            .invoke();

        logger.info("SSE Connected " + clientId);
    }

    /**
     * Checks the headers for the session cookie and extracts it when received, so we can use it on subsequent
     * tests / waits within the same session.
     */
    private void checkResponseForCookie(Response httpResponse) {
        List<Object> cookies = httpResponse.getHeaders().get("Set-Cookie");

        if (cookies != null) {
            for (Object rawCookieObj : cookies) {
                String rawCookie = rawCookieObj.toString();
                if (rawCookie.toUpperCase().contains("JSESSIONID")) {
                    this.sessionCookie = Cookie.valueOf(rawCookie);
                    break;
                }
            }
        }
    }

    public void untilEvents(Predicate<List<JSONObject>> isEvents) {
        new FluentWait<>(getEvents())
            .pollingEvery(1000, TimeUnit.MILLISECONDS)
            .withTimeout(120, TimeUnit.SECONDS)
            .ignoring(NoSuchElementException.class)
            .until( events -> isEvents.apply( events) );
    }
}

/**
 * Because neither WebTarget *or* EventSource lets you set cookies, and adding jsessionid to the URL doesn't work in
 * Jenkins 1.150.1. If you know a less-awful way to do this, please do.
 * <p>
 * Stolen from https://stackoverflow.com/questions/33626412/server-sent-event-client-with-additional-cookie and fixed
 * up a bit.
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

    /**
     * For methods that return another WebTarget, we wrap in another CAWT so we don't lose the cookie
     */
    private WebTarget wrap(WebTarget newDelegate) {
        return new CookieAddedWebTarget(newDelegate, cookie);
    }

    // All other methods from WebTarget are delegated as-is:

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
        return wrap(delegate.path(s));
    }

    @Override
    public WebTarget resolveTemplate(String s, Object o) {
        return wrap(delegate.resolveTemplate(s, o));
    }

    @Override
    public WebTarget resolveTemplate(String s, Object o, boolean b) {
        return wrap(delegate.resolveTemplate(s, o, b)); // That aughtta hold those SOBs </brockman>
    }

    @Override
    public WebTarget resolveTemplateFromEncoded(String s, Object o) {
        return wrap(delegate.resolveTemplateFromEncoded(s, o));
    }

    @Override
    public WebTarget resolveTemplates(Map<String, Object> map) {
        return wrap(delegate.resolveTemplates(map));
    }

    @Override
    public WebTarget resolveTemplates(Map<String, Object> map, boolean b) {
        return wrap(delegate.resolveTemplates(map, b));
    }

    @Override
    public WebTarget resolveTemplatesFromEncoded(Map<String, Object> map) {
        return wrap(delegate.resolveTemplatesFromEncoded(map));
    }

    @Override
    public WebTarget matrixParam(String s, Object... objects) {
        return wrap(delegate.matrixParam(s, objects));
    }

    @Override
    public WebTarget queryParam(String s, Object... objects) {
        return wrap(delegate.queryParam(s, objects));
    }

    @Override
    public Configuration getConfiguration() {
        return delegate.getConfiguration();
    }

    @Override
    public WebTarget property(String s, Object o) {
        return wrap(delegate.property(s, o));
    }

    @Override
    public WebTarget register(Class<?> aClass) {
        return wrap(delegate.register(aClass));
    }

    @Override
    public WebTarget register(Class<?> aClass, int i) {
        return wrap(delegate.register(aClass, i));
    }

    @Override
    public WebTarget register(Class<?> aClass, Class<?>... classes) {
        return wrap(delegate.register(aClass, classes));
    }

    @Override
    public WebTarget register(Class<?> aClass, Map<Class<?>, Integer> map) {
        return wrap(delegate.register(aClass, map));
    }

    @Override
    public WebTarget register(Object o) {
        return wrap(delegate.register(o));
    }

    @Override
    public WebTarget register(Object o, int i) {
        return wrap(delegate.register(o, i));
    }

    @Override
    public WebTarget register(Object o, Class<?>... classes) {
        return wrap(delegate.register(o, classes));
    }

    @Override
    public WebTarget register(Object o, Map<Class<?>, Integer> map) {
        return wrap(delegate.register(o, map));
    }

}
