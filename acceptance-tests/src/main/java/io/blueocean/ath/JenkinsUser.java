package io.blueocean.ath;

/**
 * Holds a credential to login with
 */
public class JenkinsUser {
    public final String username;
    public final String password;

    public JenkinsUser(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
