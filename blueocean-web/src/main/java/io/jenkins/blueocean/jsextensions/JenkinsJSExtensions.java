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

import hudson.PluginManager;
import hudson.PluginWrapper;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Utility class for gathering {@code jenkins-js-extension} data.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Restricted(NoExternalUse.class)
public class JenkinsJSExtensions {

    private static  final Logger LOGGER = LoggerFactory.getLogger(JenkinsJSExtensions.class);

    /**
     * The list of active plugins "as we know it". Used to determine if plugins have
     * been installed, uninstalled, deactivated etc.
     * <p>
     * We only do this because there's no jenkins mechanism for "listening" to
     * changes in the active plugin list.
     */
    private static List<PluginWrapper> lastKnownPluginList;
    /**
     * The {@code jenkins-js-extension} data for the plugins in {@code lastKnownPluginList}.
     * Starts with an empty array.
     */
    private static byte[] lastKnownPluginExtensionData = toBytes(new JSONArray());

    public static byte[] getJenkinsJSExtensionData() {
        List<PluginWrapper> activePluginList = getActivePlugins();

        if (!activePluginList.equals(lastKnownPluginList)) {
            JSONArray responseData = new JSONArray();
            // The active plugin list has changed in some way. Lets gather the info
            // for the new list.
            if (!activePluginList.isEmpty()) {
                //
                try {
                    for (PluginWrapper pluginWrapper : activePluginList) {
                        Enumeration<URL> dataResources = pluginWrapper.classLoader.getResources("jenkins-js-extension.json");
                        while (dataResources.hasMoreElements()) {
                            URL dataRes = dataResources.nextElement();
                            StringWriter fileContentBuffer = new StringWriter();

                            LOGGER.debug("Reading 'jenkins-js-extension.json' from '{}'.", dataRes);

                            try {
                                IOUtils.copy(dataRes.openStream(), fileContentBuffer, Charset.forName("UTF-8"));
                                responseData.add(JSONObject.fromObject(fileContentBuffer.toString()));
                            } catch (Exception e) {
                                LOGGER.error("Error reading 'jenkins-js-extension.json' from '" + dataRes + "'. Extensions defined in the host plugin will not be active.", e);
                            }
                        }
                    }

                } catch (IOException e) {
                    LOGGER.error("Error scanning the classpath for 'jenkins-js-extension.json' files. Plugin contributed extensions will not be active.", e);
                }
            }

            lastKnownPluginList = activePluginList;
            lastKnownPluginExtensionData = toBytes(responseData);
        }

        return lastKnownPluginExtensionData;
    }

    private static List<PluginWrapper> getActivePlugins() {
        PluginManager pluginManager = Jenkins.getActiveInstance().getPluginManager();
        List<PluginWrapper> allPlugins = pluginManager.getPlugins();
        List<PluginWrapper> activePlugins = new CopyOnWriteArrayList<>();

        for (PluginWrapper plugin : allPlugins) {
            if (plugin.isActive()) {
                activePlugins.add(plugin);
            }
        }

        return activePlugins;
    }

    private static byte[] toBytes(JSONArray jsonObjects) {
        return jsonObjects.toString().getBytes(Charset.forName("UTF-8"));
    }
}
