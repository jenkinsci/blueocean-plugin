package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.pageable.Pageable;
import io.jenkins.blueocean.rest.pageable.Pageables;
import io.jenkins.blueocean.rest.pageable.PagedResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;

import java.util.Iterator;

/**
 * Stapler-bound REST endpoint for a collection of objects.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Container<T> implements Pageable<T>, Reachable {
    /**
     * Gets the individual member by its name
     *
     * @param name identifying the member
     *
     * @return individual member
     */
    public abstract T get(String name);

    // public abstract T create(..)

    // for stapler
    public final T getDynamic(String name) {
        return get(name);
    }

    /**
     * Base implementation of pagination that is dumb.
     */
    @Override
    public Iterator<T> iterator(int start, int limit) {
        return Pageables.slice(iterator(),start,limit);
    }

    /**
     * When GET is requested on '/', serve the collection
     * @return collection in this container
     */
    @WebMethod(name="") @GET @PagedResponse
    // if we wanted collection listing to take filtering parameters, we can do that with one additional parameter
    public Pageable<T> list(/*@QueryParameter Query q*/) {
        return this;
    }
}
