package io.jenkins.blueocean.rest.model;

import hudson.Util;
import io.jenkins.blueocean.rest.hal.Link;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.verb.POST;

import java.util.Collection;

public abstract class BluePlugin extends Resource {

    public static final String ID = "id";
    public static final String LIFECYCLE = "lifecycle";
    public static final String STATE = "state";
    public static final String DISPLAY_NAME = "displayName";
    public static final String DESCRIPTION = "description";
    public static final String VERSION = "version";
    public static final String LATEST_VERSION = "latestVersion";
    public static final String URL = "url";
    public static final String REQUIRED_CORE = "requiredCore";
    public static final String COMPATIBLE_SINCE = "compatibleSince";
    public static final String CATEGORIES = "categories";
    public static final String DEPENDENCIES = "dependencies";
    public static final String SUPPORTS_DYNAMIC_LOADING = "supportsDynamicLoading";
    public static final String METHOD_INSTALL = "install";
    public static final String METHOD_REMOVE = "remove";
    public static final String METHOD_ENABLE = "enable";
    public static final String METHOD_DISABLE = "disable";
    private final Link parent;

    public BluePlugin(Link parent) {
        this.parent = parent;
    }

    public enum Lifecycle {
        /** Available for installation from the update center */
        AVAILABLE,
        /** New version is available from the update center */
        UPDATE_AVAILABLE,
        /** Installed but no update available */
        INSTALLED;
    }

    public enum State {
        /** Plugin is installed and enabled */
        ENABLED,
        /** Plugin is installed but disabled */
        DISABLED,
        /** Plugin not in an applicable {@link Lifecycle} */
        UNKNOWN;
    }

    @Exported(name = ID)
    public abstract String getId();

    @Exported(name = LIFECYCLE)
    public abstract Lifecycle getPluginLifecycle();

    @Exported(name = STATE)
    public abstract State getPluginState();

    @Exported(name = DISPLAY_NAME)
    public abstract String getDisplayName();

    @Exported(name = DESCRIPTION)
    public abstract String getDescription();

    @Exported(name = VERSION)
    public abstract String getVersion();

    @Exported(name = LATEST_VERSION)
    public abstract String getLatestVersion();

    @Exported(name = URL)
    public abstract String getURL();

    @Exported(name = REQUIRED_CORE)
    public abstract String getRequiredCore();

    @Exported(name = COMPATIBLE_SINCE)
    public abstract String getCompatibleSinceVersion();

    @Exported(name = CATEGORIES)
    public abstract Collection<String> getCategories();

    @Exported(name = DEPENDENCIES)
    public abstract Collection<BluePluginDependency> getDependencies();

    @Override
    public final Link getLink() {
        return parent.rel(Util.rawEncode(getId()));
    }

    @POST @WebMethod(name = METHOD_INSTALL)
    public abstract void install();

    @POST @WebMethod(name = METHOD_REMOVE)
    public abstract void remove();

    @POST @WebMethod(name = METHOD_ENABLE)
    public abstract void enable();

    @POST @WebMethod(name = METHOD_DISABLE)
    public abstract void disable();
}
