package io.jenkins.blueocean.security;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Ivan Meredith
 */
final public class PasswordCredentials implements Credentials {
    @JsonProperty("user")
    final public String user;

    @JsonProperty("password")
    final public String password;

    public PasswordCredentials(@JsonProperty("user") String user, @JsonProperty("password") String password) {
        this.user = user;
        this.password = password;
    }
}
