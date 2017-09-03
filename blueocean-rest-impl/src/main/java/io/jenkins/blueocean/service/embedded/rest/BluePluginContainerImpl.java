package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.model.UpdateCenter;
import hudson.model.UpdateSite;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePlugin;
import io.jenkins.blueocean.rest.model.BluePluginContainer;
import io.jenkins.blueocean.rest.model.BluePluginDependency;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BluePluginContainerImpl extends BluePluginContainer {

    private final BlueOrganization organization;

    public BluePluginContainerImpl(BlueOrganization organization) {
        this.organization = organization;
    }

    @Override
    public BluePlugin get(final String name) {
        return Iterators.find(iterator(), new Predicate<BluePlugin>() {
            @Override
            public boolean apply(@Nullable BluePlugin input) {
                return input != null && input.getId().equals(name);
            }
        }, null);
    }

    @Override
    public Link getLink() {
        return organization.getLink().rel("plugins");
    }

    @Override
    public Iterator<BluePlugin> iterator() {
        Jenkins jenkins = Jenkins.getInstance();
        UpdateCenter updateCenter = jenkins.getUpdateCenter();

        Map<String, MutableBluePlugin> plugins = Maps.newHashMap();
        for (UpdateSite.Plugin plugin : updateCenter.getAvailables()) {
            MutableBluePlugin model = new MutableBluePlugin(plugins, getLink());
            model.setPluginMetadata(plugin);
            plugins.put(model.getId(), model);
        }

        PluginManager pluginManager = jenkins.getPluginManager();
        for (PluginWrapper plugin : pluginManager.getPlugins()) {
            MutableBluePlugin model = plugins.get(plugin.getShortName());
            if (model == null) {
                model = new MutableBluePlugin(plugins, getLink());
            }
            model.setPlugin(plugin);
            plugins.put(model.getId(), model);
        }

        return filter(Iterators.transform(plugins.values().iterator(), new Function<MutableBluePlugin, BluePlugin>() {
            @Override
            public BluePlugin apply(MutableBluePlugin input) {
                return new BluePluginImpl(input, getLink());
            }
        }));
    }

    static Iterator<BluePlugin> filter(Iterator<BluePlugin> plugins) {

        StaplerRequest req = Stapler.getCurrentRequest();
        if (req == null) {
            return plugins;
        }

        String lifecycle = req.getParameter("lifecycle");
        if (lifecycle == null) {
            return plugins;
        }

        final Collection<String> requestedLifecycles = Arrays.asList(StringUtils.split(lifecycle, ","));

        return Iterators.filter(plugins, new Predicate<BluePlugin>() {
            @Override
            public boolean apply(@Nullable BluePlugin input) {
                return input != null && requestedLifecycles.contains(input.getPluginLifecycle().name());
            }
        });
    }

    final static class MutableBluePlugin extends BluePlugin {

        private UpdateSite.Plugin pluginMetadata;
        private PluginWrapper plugin;
        private final Map<String, MutableBluePlugin> allPlugins;

        public MutableBluePlugin(Map<String, MutableBluePlugin> allPlugins, Link parent) {
            super(parent);
            this.allPlugins = allPlugins;
        }

        public void setPluginMetadata(UpdateSite.Plugin pluginMetadata) {
            this.pluginMetadata = pluginMetadata;
        }

        public void setPlugin(PluginWrapper plugin) {
            this.plugin = plugin;
        }

        @Override
        public String getId() {
            if (pluginMetadata != null) {
                return pluginMetadata.name;
            } else {
                return plugin.getShortName();
            }
        }

        @Override
        public Lifecycle getPluginLifecycle() {
            if (plugin == null) {
                return Lifecycle.AVAILABLE;
            } else if (plugin.hasUpdate()) {
                return Lifecycle.UPDATE_AVAILABLE;
            } else {
                return Lifecycle.INSTALLED;
            }
        }

        @Override
        public State getPluginState() {
            if (plugin != null && plugin.isEnabled()) {
                return State.ENABLED;
            } else if (plugin != null && !plugin.isEnabled()) {
                return State.DISABLED;
            } else {
                return State.UNKNOWN;
            }
        }

        @Override
        public String getDisplayName() {
            return pluginMetadata != null ? pluginMetadata.getDisplayName() : plugin.getDisplayName();
        }

        @Override
        public String getDescription() {
            return pluginMetadata != null ? pluginMetadata.excerpt : null;
        }

        @Override
        public String getVersion() {
            return pluginMetadata.version != null ? pluginMetadata.getDisplayName() : plugin.getDisplayName();
        }

        @Override
        public String getLatestVersion() {
            return pluginMetadata != null ? pluginMetadata.version : plugin.getVersion();
        }

        @Override
        public String getURL() {
            return pluginMetadata != null ? pluginMetadata.url : plugin.getUrl();
        }

        @Override
        public String getRequiredCore() {
            return pluginMetadata != null ? pluginMetadata.requiredCore : null;
        }

        @Override
        public String getCompatibleSinceVersion() {
            return pluginMetadata != null ? pluginMetadata.compatibleSinceVersion : null;
        }

        @Override
        public Collection<String> getCategories() {
            return pluginMetadata != null && pluginMetadata.categories != null ? Arrays.asList(pluginMetadata.categories) : null;
        }

        @Override
        public void install() {
            Jenkins.getInstance().getPluginManager().install(ImmutableList.of(pluginMetadata.name), true);
        }

        @Override
        public void remove() {
            if (plugin == null) {
                throw new ServiceException.BadRequestException("installed");
            }
            try {
                plugin.doDoUninstall();
            } catch (IOException e) {
                throw new ServiceException.UnexpectedErrorException(String.format("Could not remove %s", getDisplayName()), e);
            }
        }

        @Override
        public void enable() {
            if (plugin.isEnabled()) {
                throw new ServiceException.BadRequestException(String.format("%s is already enabled", getDisplayName()));
            }
            try {
                plugin.enable();
            } catch (IOException e) {
                throw new ServiceException.UnexpectedErrorException(e.getMessage(), e);
            }
        }

        @Override
        public void disable() {
            if (!plugin.isEnabled()) {
                throw new ServiceException.BadRequestException(String.format("%s is already disabled", getDisplayName()));
            }
            try {
                plugin.disable();
            } catch (IOException e) {
                throw new ServiceException.UnexpectedErrorException(String.format("%s is already disabled", getDisplayName()), e);
            }
        }

        @Override
        public Collection<BluePluginDependency> getDependencies() {
            List<BluePluginDependency> dependencies = Lists.newLinkedList();
            if (plugin == null) {
                if (pluginMetadata.dependencies != null) {
                    Collection<String> deps = pluginMetadata.dependencies.values();
                    dependencies.addAll(Collections2.transform(deps, new Function<String, BluePluginDependency>() {
                        @Override
                        public BluePluginDependency apply(String input) {
                            return new PluginDependencyImpl(BluePluginDependency.Type.REQUIRED, allPlugins.get(input));
                        }
                    }));
                }
                if (pluginMetadata.optionalDependencies != null) {
                    Collection<String> optionalDependencies = pluginMetadata.optionalDependencies.keySet();
                    dependencies.addAll(Collections2.transform(optionalDependencies, new Function<String, BluePluginDependency>() {
                        @Override
                        public BluePluginDependency apply(String input) {
                            return new PluginDependencyImpl(BluePluginDependency.Type.OPTIONAL, allPlugins.get(input));
                        }
                    }));
                }
            } else {
                List<PluginWrapper.Dependency> deps = plugin.getDependencies();
                if (deps != null) {
                    dependencies.addAll(Collections2.transform(deps, new Function<PluginWrapper.Dependency, BluePluginDependency>() {
                        @Override
                        public BluePluginDependency apply(PluginWrapper.Dependency input) {
                            return new PluginDependencyImpl(BluePluginDependency.Type.REQUIRED, allPlugins.get(input.shortName));
                        }
                    }));
                }
                List<PluginWrapper.Dependency> optionalDependencies = plugin.getOptionalDependencies();
                if (optionalDependencies != null) {
                    dependencies.addAll(Collections2.transform(optionalDependencies, new Function<PluginWrapper.Dependency, BluePluginDependency>() {
                        @Override
                        public BluePluginDependency apply(PluginWrapper.Dependency input) {
                            return new PluginDependencyImpl(BluePluginDependency.Type.OPTIONAL, allPlugins.get(input.shortName));
                        }
                    }));
                }
            }
            return dependencies;
        }
    }

    public static final class PluginDependencyImpl extends BluePluginDependency {
        private final Type type;
        private final BluePlugin plugin;

        PluginDependencyImpl(Type type, BluePlugin plugin) {
            this.type = type;
            this.plugin = plugin;
        }

        @Override
        public BluePlugin getPlugin() {
            return plugin;
        }

        @Override
        public Type getType() {
            return type;
        }
    }
}
