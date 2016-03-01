package io.jenkins.blueocean.rest.model;

/**
 * @author Kohsuke Kawaguchi
 */
public class GenericResource<T> extends Resource {
    private final T value;

    public GenericResource(T value) {
        this.value = value;
    }

    @Override
    public Object getState() {
        return value;
    }

    // TODO: allow 'value' to expose additional routes
}
