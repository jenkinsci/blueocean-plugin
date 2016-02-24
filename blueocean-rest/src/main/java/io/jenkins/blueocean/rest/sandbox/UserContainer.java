package io.jenkins.blueocean.rest.sandbox;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.api.profile.CreateUserRequest;
import io.jenkins.blueocean.commons.stapler.JsonBody;
import io.jenkins.blueocean.rest.ApiRoutable;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.POST;

import java.util.Iterator;

/**
 * User API.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public abstract class UserContainer extends Container<User> implements ApiRoutable, ExtensionPoint {
    /**
     * Creates a new user.
     */
    @WebMethod(name="") @POST
    public abstract User create(@JsonBody CreateUserRequest req);

    @Override
    public final String getUrlName() {
        return "users";
    }

    /**
     * Most {@link UserContainer}s will be unlikely to support iteration.
     */
    @Override
    public Iterator<User> iterator() {
        throw new UnsupportedOperationException();
    }
}
