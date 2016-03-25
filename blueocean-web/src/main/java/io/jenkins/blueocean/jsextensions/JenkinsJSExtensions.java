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
package io.jenkins.blueocean.jsextensions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.PluginWrapper;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Utility class for gathering {@code jenkins-js-extension} data.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Restricted(NoExternalUse.class)
public class JenkinsJSExtensions {
    public static final JenkinsJSExtensions INSTANCE = new JenkinsJSExtensions();
    private static  final Logger LOGGER = LoggerFactory.getLogger(JenkinsJSExtensions.class);
    private final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * The list of active pluginCache "as we know it". Used to determine if pluginCache have
     * been installed, uninstalled, deactivated etc.
     * <p>
     * We only do this because there's no jenkins mechanism for "listening" to
     * changes in the active plugin list.
     */
    private final List<PluginWrapper> pluginCache = new CopyOnWriteArrayList<>();

    private JenkinsJSExtensions() {
    }


    private final Map<String, Map> jsExtensionCache = new ConcurrentHashMap<>();


    public byte[] getJenkinsJSExtensionData() {
        try {
            refreshCacheIfNeeded();
            return mapper.writeValueAsBytes(jsExtensionCache.values());
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize to JSON: "+e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String getGav(Map ext){
        return ext.get("hpiPluginId") != null ? (String)ext.get("hpiPluginId") : null;
    }

    private void refreshCacheIfNeeded(){
        List<PluginWrapper> latestPlugins = Jenkins.getActiveInstance().getPluginManager().getPlugins();
        if(!latestPlugins.equals(pluginCache)){
            refreshCache(latestPlugins);
        }
    }
    private synchronized void refreshCache(List<PluginWrapper> latestPlugins){
        if(!latestPlugins.equals(pluginCache)) {
            pluginCache.clear();
            pluginCache.addAll(latestPlugins);
            refreshCache(pluginCache);
        }
        for (PluginWrapper pluginWrapper : pluginCache) {
            //skip probing plugin if already read
            if (jsExtensionCache.get(pluginWrapper.getLongName()) != null) {
                continue;
            }
            try {
                Enumeration<URL> dataResources = pluginWrapper.classLoader.getResources("jenkins-js-extension.json");
                while (dataResources.hasMoreElements()) {
                    URL dataRes = dataResources.nextElement();
                    StringWriter fileContentBuffer = new StringWriter();

                    LOGGER.debug("Reading 'jenkins-js-extension.json' from '{}'.", dataRes);

                    try {
                        IOUtils.copy(dataRes.openStream(), fileContentBuffer, Charset.forName("UTF-8"));
                        Map ext = mapper.readValue(dataRes.openStream(), Map.class);
                        String pluginId = getGav(ext);
                        if (pluginId != null) {
                            jsExtensionCache.put(pluginId, ext);
                        } else {
                            LOGGER.error(String.format("Plugin %s JS extension has missing hpiPluginId", pluginWrapper.getLongName()));
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error reading 'jenkins-js-extension.json' from '" + dataRes + "'. Extensions defined in the host plugin will not be active.", e);
                    }
                }
            } catch (IOException e) {
                LOGGER.error(String.format("Error locating jenkins-js-extension.json for plugin %s", pluginWrapper.getLongName()));
            }
        }
    }

}
