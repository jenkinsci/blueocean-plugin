package io.jenkins.blueocean.auth.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicate;
import io.jenkins.blueocean.security.Credentials;

import javax.annotation.Nullable;

/**
 * In the github oauth plugin
 */
public final class GithubCredentials implements Credentials {
    @JsonProperty("login")
    public final String login;
    @JsonProperty("accessToken")
    public final String accessToken;

    public GithubCredentials(
        @JsonProperty("login") String login,
        @JsonProperty("accessToken") String accessToken) {
        this.login = login;
        this.accessToken = accessToken;
    }

    @Override
    public Predicate<Credentials> identityPredicate() {
        final GithubCredentials me = this;
        return new Predicate<Credentials>() {
            @Override
            public boolean apply(@Nullable Credentials input) {
                if (input instanceof GithubCredentials) {
                    GithubCredentials other = (GithubCredentials)input;
                    return me.login.equals(other.login);
                }
                return false;
            }
        };
    }
}
