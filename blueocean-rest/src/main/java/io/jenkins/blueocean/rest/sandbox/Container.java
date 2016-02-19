package io.jenkins.blueocean.rest.sandbox;

/**
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
}
