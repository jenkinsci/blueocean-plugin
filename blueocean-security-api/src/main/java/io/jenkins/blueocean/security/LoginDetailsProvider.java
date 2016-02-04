package io.jenkins.blueocean.security;

/**
 * Created by ivan on 9/02/16.
 */
public interface LoginDetailsProvider<T extends LoginDetails> {
    Class<T> getLoginDetalsClass();

    Identity authenticate(T loginDetails);
}
