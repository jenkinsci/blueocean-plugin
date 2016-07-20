package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;

import java.util.Map;

/**
 * Map of {@link BlueExtensionClass}
 *
 * @author Vivek Pandey
 */
public abstract class BlueExtensionClassMap extends Resource{

    @Exported(name = "map",inline = true)
    public abstract Map<String, BlueExtensionClass> getMap();
}
