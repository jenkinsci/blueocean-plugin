package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.export.Exported;

import java.util.Collection;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_EXTENSION_CLASS;

/**
 * Abstraction that defines a class extending Jenkins
 *
 * @author Vivek Pandey
 */
@Capability(BLUE_EXTENSION_CLASS)
public abstract class BlueExtensionClass extends Resource {
    private static final String CLASSES="classes";

    /**
     *
     * @return classes known to this extension class. If given extension class is not known then empty collection is returned.
     */
    @Exported(name = CLASSES)
    public abstract Collection<String> getClasses();
}
