package io.jenkins.blueocean.rest.sandbox;

import com.google.common.collect.Iterables;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.Query;
import io.jenkins.blueocean.rest.pageable.Pageable;
import io.jenkins.blueocean.rest.pageable.Pageables;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;

import java.util.Iterator;

/**
 * Stapler-bound REST endpoint for a collection of objects.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Container<T> implements Pageable<T> {
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
     * Base implementation of pagenation that is dumb.
     */
    @Override
    public Iterator<T> iterator(int start, int limit) {
        return Pageables.slice(iterator(),start,limit);
    }

    /**
     * When GET is requested on '/', serve the collection
     */
    @WebMethod(name="") @GET @TreeResponse
    // if we wanted collection listing to take filtering parameters, we can do that with one additional parameter
    public Object[] list(/*@QueryParameter Query q*/) {
        // TODO: pagenation
        return Iterables.toArray(this,Object.class);
    }
}
