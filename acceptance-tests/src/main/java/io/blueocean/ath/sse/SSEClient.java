package io.blueocean.ath.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.offbytwo.jenkins.JenkinsServer;
import io.blueocean.ath.BaseUrl;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.SseFeature;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Singleton
public class SSEClient {

    @Inject @BaseUrl
    String baseUrl;

    ObjectMapper mapper = new ObjectMapper();

    List<JenkinsEvent> events = Lists.newArrayList();
    private EventListener listener = inboundEvent -> {
        try {
            JenkinsEvent jenkinsEvent = mapper.readValue(inboundEvent.readData(), JenkinsEvent.class);
            events.add(jenkinsEvent);
        } catch (IOException e) {
            e.printStackTrace();
        }

    };
    public void connect() throws UnirestException, InterruptedException {
        HttpResponse<JsonNode> httpResponse = Unirest.get(baseUrl + "/sse-gateway/connect?clientId=ath").asJson();
        JsonNode body = httpResponse.getBody();
        Client client = ClientBuilder.newBuilder().register(SseFeature.class).build();
        WebTarget target = client.target(baseUrl + "/sse-gateway/listen/ath;jsessionid="+body.getObject().getJSONObject("data").getString("jsessionid"));
        EventSource source = EventSource.target(target).build();
        source.register(listener);
        source.open();

        JSONObject req = new JSONObject()
            .put("dispatcherId","ath")
            .put("subscribe", new JSONArray(ImmutableList.of(
                new JSONObject().put("jenkins_org", "jenkins")
                    .put("jenkins_channel", "job"))))
            .put("unsubscribe", new JSONArray());

        Unirest.post(baseUrl + "/sse-gateway/configure?batchId=1")
            .body(req).asJson();
    }
}
