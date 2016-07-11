package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.export.Exported;

import java.util.Collection;

/**
 * Abstraction that defines a class extending Jenkins
 *
 * @author Vivek Pandey
 */
@Capability("io.jenkins.blueocean.rest.model.BlueExtensionClass")
public abstract class BlueExtensionClass extends Resource {
    private static final String CLASSES="classes";

    /**
     *
     * @return classes known to this extension class
     */
    @Exported(name = CLASSES)
    public abstract Collection<String> getClasses();
}
