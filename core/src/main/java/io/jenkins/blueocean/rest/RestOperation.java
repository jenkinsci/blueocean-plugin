package io.jenkins.blueocean.rest;

import com.google.inject.Inject;
import io.jenkins.blueocean.UserPrincipal;
import io.jenkins.embryo.App;

/**
 * Abstract class that all REST API implementations must implement.
 *
 * @author Vivek Pandey
 */
public abstract class RestOperation<R,S> extends App {
    @Inject
    private UserPrincipal principal;


    protected UserPrincipal getPrincipal() {
        return principal;
    }

    public abstract S execute(R request);

}
