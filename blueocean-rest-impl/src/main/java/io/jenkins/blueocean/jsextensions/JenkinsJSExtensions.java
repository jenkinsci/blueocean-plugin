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

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.io.IOUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import hudson.Extension;
import hudson.PluginWrapper;
import hudson.util.HttpResponses;
import io.jenkins.blueocean.RootRoutable;
import io.jenkins.blueocean.rest.model.BlueExtensionClass;
import io.jenkins.blueocean.rest.model.BlueExtensionClassContainer;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;

/**
 * Utility class for gathering {@code jenkins-js-extension} data.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Extension
@Restricted(NoExternalUse.class)
@SuppressWarnings({"rawtypes","unchecked"})
public class JenkinsJSExtensions implements RootRoutable {
    private static  final Logger LOGGER = LoggerFactory.getLogger(JenkinsJSExtensions.class);
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

    public JenkinsJSExtensions() {
    }

    /**
     * For the location in the API: /blue/js-extensions
     */
    @Override
    public String getUrlName() {
        return "js-extensions";
    }

    /**
     * Return the actual data, from /js-extensions
     */
    @WebMethod(name="") @GET
    public HttpResponse doData() {
        Object jsExtensionData = getJenkinsJSExtensionData();
        JSONArray jsExtensionDataJson = JSONArray.fromObject(jsExtensionData);
        return HttpResponses.okJSON(jsExtensionDataJson);
    }

    /*protected*/ static Collection<Object> getJenkinsJSExtensionData() {
        refreshCacheIfNeeded();
        return jsExtensionCache.values();
    }

    private static String getGav(Map ext){
        return ext.get("hpiPluginId") != null ? (String)ext.get("hpiPluginId") : null;
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
                        Map<?,List<Map>> extensionData = mapper.readValue(dataRes.openStream(), Map.class);
                        List<Map> extensions = (List<Map>)extensionData.get("extensions");
                        for (Map extension : extensions) {
                            try {
                                String type = (String)extension.get("type");
                                if (type != null) {
                                    BlueExtensionClassContainer extensionClassContainer
                                        = Jenkins.getInstance().getExtensionList(BlueExtensionClassContainer.class).get(0);
                                    Map classInfo = (Map)mergeObjects(extensionClassContainer.get(type));
                                    List classInfoClasses = (List)classInfo.get("_classes");
                                    classInfoClasses.add(0, type);
                                    extension.put("_class", type);
                                    extension.put("_classes", classInfoClasses);
                                }
                            } catch(Exception e) {
                                LOGGER.error("An error occurred when attempting to read type information from jenkins-js-extension.json from: " + dataRes, e);
                            }
                        }
                        String pluginId = getGav(extensionData);
                        if (pluginId != null) {
                            jsExtensionCache.put(pluginId, mergeObjects(extensionData));
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
