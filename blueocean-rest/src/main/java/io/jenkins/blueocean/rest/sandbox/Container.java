package io.jenkins.blueocean.rest.sandbox;

import com.google.common.collect.Iterables;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;

/**
 * Stapler-bound REST endpoint for a collection of objects.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Container<T> implements Iterable<T> {
    /**
     * Gets the individual member by its name
     */
    public abstract T get(String name);

    // public abstract T create(..)

    // for stapler
    public final T getDynamic(String name) {
        return get(name);
    }

    /**
     * When GET is requested on '/', serve the collection
     */
    @WebMethod(name="") @GET @TreeResponse
    public Object[] list() {
        // TODO: pagenation
        return Iterables.toArray(this,Object.class);
    }
}
