package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePlugin;
import io.jenkins.blueocean.rest.model.BluePluginContainer;
import io.jenkins.blueocean.rest.model.BluePluginDependency;
import jenkins.model.Jenkins;

import java.util.Collection;

/**
 * Immutable Plugin implementation
 */
public class BluePluginImpl extends BluePlugin {

    private final BluePlugin plugin;

    BluePluginImpl(BluePlugin plugin, Link parent) {
        super(parent);
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return plugin.getId();
    }

    @Override
    public State getPluginState() {
        return plugin.getPluginState();
    }

    @Override
    public Lifecycle getPluginLifecycle() {
        return plugin.getPluginLifecycle();
    }

    @Override
    public String getDisplayName() {
        return plugin.getDisplayName();
    }

    @Override
    public String getDescription() {
        return plugin.getDescription();
    }

    @Override
    public String getVersion() {
        return plugin.getVersion();
    }

    @Override
    public String getLatestVersion() {
        return plugin.getLatestVersion();
    }

    @Override
    public String getURL() {
        return plugin.getURL();
    }

    @Override
    public String getRequiredCore() {
        return plugin.getRequiredCore();
    }

    @Override
    public String getCompatibleSinceVersion() {
        return plugin.getCompatibleSinceVersion();
    }

    @Override
    public Collection<String> getCategories() {
        return plugin.getCategories();
    }

    @Override
    @Navigable
    public Collection<BluePluginDependency> getDependencies() {
        return plugin.getDependencies();
    }

    @Override
    public void install() {
        switch (plugin.getPluginLifecycle()) {
            case INSTALLED:
                throw new ServiceException.BadRequestException(String.format("%s is already installed", getDisplayName()));
            case AVAILABLE:
            case UPDATE_AVAILABLE:
                plugin.install();
            default:
                throw new ServiceException.UnexpectedErrorException(String.format("Plugin is in unknown lifecycle state %s", plugin.getPluginLifecycle()));
        }
    }

    @Override
    public void remove() {
        plugin.remove();
    }

    @Override
    public void enable() {
        plugin.enable();
    }

    @Override
    public void disable() {
        plugin.disable();
    }
}
