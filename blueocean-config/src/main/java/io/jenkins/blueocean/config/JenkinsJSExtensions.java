/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.blueocean.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import hudson.Extension;
import hudson.PluginWrapper;
import io.jenkins.blueocean.rest.model.BlueExtensionClass;
import io.jenkins.blueocean.rest.model.BlueExtensionClassContainer;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;

/**
 * Utility class for gathering {@code jenkins-js-extension} data.
 */
@Extension
@Restricted(NoExternalUse.class)
@SuppressWarnings({"rawtypes","unchecked"})
public class JenkinsJSExtensions {

    private static  final Logger LOGGER = LoggerFactory.getLogger(JenkinsJSExtensions.class);

    private static final String PLUGIN_ID = "hpiPluginId";
    private static final String PLUGIN_VER = "hpiPluginVer";
    private static final String PLUGIN_EXT = "extensions";

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * The list of active pluginCache "as we know it". Used to determine if pluginCache have
     * been installed, uninstalled, deactivated etc.
     * <p>
     * We only do this because there's no jenkins mechanism for "listening" to
     * changes in the active plugin list.
     */
    private static final List<PluginWrapper> pluginCache = new CopyOnWriteArrayList<>();

    private static final Map<String, Object> jsExtensionCache = new ConcurrentHashMap<>();

    /**
     * Return the actual data, from /js-extensions
     */
    public static JSONArray getExtensionsData() {
        Object jsExtensionData = getJenkinsJSExtensionData();
        JSONArray jsExtensionDataJson = JSONArray.fromObject(jsExtensionData);
        return jsExtensionDataJson;
    }

    /*protected*/ static Collection<Object> getJenkinsJSExtensionData() {
        refreshCacheIfNeeded();
        return jsExtensionCache.values();
    }

    private static String getGav(Map ext){
        return (String) ext.get(PLUGIN_ID);
    }

    private static void refreshCacheIfNeeded(){
        List<PluginWrapper> latestPlugins = Jenkins.getInstance().getPluginManager().getPlugins();
        if(!latestPlugins.equals(pluginCache)){
            refreshCache(latestPlugins);
        }
    }
    private synchronized static void refreshCache(List<PluginWrapper> latestPlugins){
        if(!latestPlugins.equals(pluginCache)) {
            pluginCache.clear();
            pluginCache.addAll(latestPlugins);
            refreshCache(pluginCache);
        }
        for (PluginWrapper pluginWrapper : pluginCache) {
            //skip if not active
            if (!pluginWrapper.isActive()) {
                continue;
            }
            //skip probing plugin if already read
            if (jsExtensionCache.get(pluginWrapper.getLongName()) != null) {
                continue;
            }
            try {
                Enumeration<URL> dataResources = pluginWrapper.classLoader.getResources("jenkins-js-extension.json");
                boolean hasDefinedExtensions = false;

                while (dataResources.hasMoreElements()) {
                    URL dataRes = dataResources.nextElement();

                    LOGGER.debug("Reading 'jenkins-js-extension.json' from '{}'.", dataRes);

                    try (InputStream dataResStream = dataRes.openStream()) {
                        Map<String, Object> extensionData = mapper.readValue(dataResStream, Map.class);

                        String pluginId = getGav(extensionData);
                        if (pluginId != null) {
                            // Skip if the plugin name specified on the extension data does not match the name
                            // on the PluginWrapper for this iteration. This can happen for e.g. aggregator
                            // plugins, in which case you'll be seeing extension resources on it's dependencies.
                            // We can skip these here because we will process those plugins themselves in a
                            // future/past iteration of this loop.
                            if (!pluginId.equals(pluginWrapper.getShortName())) {
                                continue;
                            }
                        } else {
                            LOGGER.error(String.format("Plugin %s JS extension has missing hpiPluginId", pluginWrapper.getLongName()));
                            continue;
                        }

                        List<Map> extensions = (List<Map>) extensionData.get(PLUGIN_EXT);
                        for (Map extension : extensions) {
                            try {
                                String type = (String) extension.get("type");
                                if (type != null) {
                                    BlueExtensionClassContainer extensionClassContainer
                                        = Jenkins.getInstance().getExtensionList(BlueExtensionClassContainer.class).get(0);
                                    Map classInfo = (Map) mergeObjects(extensionClassContainer.get(type));
                                    List classInfoClasses = (List) classInfo.get("_classes");
                                    classInfoClasses.add(0, type);
                                    extension.put("_class", type);
                                    extension.put("_classes", classInfoClasses);
                                }
                            } catch (Exception e) {
                                LOGGER.error("An error occurred when attempting to read type information from jenkins-js-extension.json from: " + dataRes, e);
                            }
                        }

                        extensionData.put(PLUGIN_VER, pluginWrapper.getVersion());
                        jsExtensionCache.put(pluginId, mergeObjects(extensionData));
                        hasDefinedExtensions = true;
                    }
                }

                if (!hasDefinedExtensions) {
                    // Manufacture an entry for all plugins that do not have any defined
                    // extensions. This adds some info about the plugin that the UI might
                    // need access to e.g. the plugin version.
                    Map<String, Object> extensionData = new LinkedHashMap<>();
                    extensionData.put(PLUGIN_ID, pluginWrapper.getShortName());
                    extensionData.put(PLUGIN_VER, pluginWrapper.getVersion());
                    extensionData.put(PLUGIN_EXT, Collections.emptyList());
                    jsExtensionCache.put(pluginWrapper.getShortName(), mergeObjects(extensionData));
                }
            } catch (IOException e) {
                LOGGER.error(String.format("Error locating jenkins-js-extension.json for plugin %s", pluginWrapper.getLongName()));
            }
        }
    }

    //
    // ***********************************************************************************************************
    // TODO: Someone needs to write some docs on this function, explaining what it is doing and why it's needed.
    // ***********************************************************************************************************
    //
    private static Object mergeObjects(Object incoming) {
        if (incoming instanceof Map) {
            Map m = new HashMap();
            Map in = (Map)incoming;
            for (Object key : in.keySet()) {
                Object value = mergeObjects(in.get(key));
                m.put(key, value);
            }
            return m;
        }
        if (incoming instanceof Collection) {
            List l = new ArrayList();
            for (Object i : (Collection)incoming) {
                i = mergeObjects(i);
                l.add(i);
            }
            return l;
        }
        if (incoming instanceof Class) {
            return ((Class) incoming).getName();
        }
        if (incoming instanceof BlueExtensionClass) {
            BlueExtensionClass in = (BlueExtensionClass)incoming;
            Map m = new HashMap();
            Object value = mergeObjects(in.getClasses());
            m.put("_classes", value);
            return m;
        }
        return incoming;
    }
}
