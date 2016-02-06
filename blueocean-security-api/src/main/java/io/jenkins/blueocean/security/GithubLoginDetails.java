package io.jenkins.blueocean.security;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * In the github oauth plugin
 */
public final class GithubLoginDetails implements LoginDetails {
    @JsonProperty("login")
    public final String login;
    @JsonProperty("accessToken")
    public final String accessToken;

    public GithubLoginDetails(
        @JsonProperty("login") String login,
        @JsonProperty("accessToken") String accessToken) {
        this.login = login;
        this.accessToken = accessToken;
    }
}
