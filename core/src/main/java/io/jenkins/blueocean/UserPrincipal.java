package io.jenkins.blueocean;

/**
 * User Principal to be set after successful authentication via auth filter
 *
 * TODO: Should carry roles, token scope and all details related to the identity
 *
 * @author Vivek Pandey
 */
public class UserPrincipal {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
