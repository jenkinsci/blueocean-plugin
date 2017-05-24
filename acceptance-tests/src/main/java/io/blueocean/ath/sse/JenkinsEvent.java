package io.blueocean.ath.sse;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JenkinsEvent {
    @JsonProperty("jenkins_event")
    public String event;
}
