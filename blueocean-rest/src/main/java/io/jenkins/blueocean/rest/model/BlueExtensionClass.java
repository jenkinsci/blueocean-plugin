package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;

import java.util.Collection;

/**
 * Abstraction that defines a class extending Jenkins
 *
 * @author Vivek Pandey
 */
public abstract class BlueExtensionClass extends Resource {
    private static final String CLASSES="classes";

    /**
     *
     * @return classes known to this extension class
     */
    @Exported(name = CLASSES)
    public abstract Collection<String> getClasses();
}
