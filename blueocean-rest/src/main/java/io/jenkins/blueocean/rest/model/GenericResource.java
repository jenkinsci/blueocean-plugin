package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public class GenericResource<T> extends Resource {
    private final T value;

    public GenericResource(T value) {
        this.value = value;
    }

    @Exported(merge = true)
    @Override
    public Object getState() {
        return value;
    }

    // TODO: allow 'value' to expose additional routes
}
