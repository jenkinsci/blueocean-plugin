package io.jenkins.blueocean.security;

/**
 * Created by ivan on 9/02/16.
 */
public class PasswordLoginDetails implements LoginDetails {
    private String user;
    private String password;

    public PasswordLoginDetails(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
