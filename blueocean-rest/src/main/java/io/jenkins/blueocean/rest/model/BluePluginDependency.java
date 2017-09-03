package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class BluePluginDependency {

    public static final String PLUGIN = "plugin";
    public static final String TYPE = "type";

    public enum Type {
        REQUIRED,
        OPTIONAL
    }

    @Exported(name = PLUGIN)
    public abstract BluePlugin getPlugin();

    @Exported(name = TYPE)
    public abstract Type getType();
}
