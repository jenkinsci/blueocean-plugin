package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.rest.Capabilities;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueExtensionClass;

import java.util.Set;

/**
 * @author Vivek Pandey
 */
public class ExtensionClassImpl extends BlueExtensionClass {
    private final Class baseClass;
    private final Reachable parent;

    public ExtensionClassImpl(Class baseClass, Reachable parent) {
        this.baseClass = baseClass;
        this.parent = parent;
    }

    @Override
    public Set<String> getClasses() {
        return Capabilities.allSuperClassesAndCapabilities(baseClass);
    }

    public Link getLink() {
        return parent.getLink().rel(baseClass.getName());
    }
}
