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

import io.jenkins.blueocean.service.embedded.BaseTest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JenkinsJSExtensionsTest extends BaseTest {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void test() {
        // Simple test of JenkinsJSExtensions. It should find the "blueocean-dashboard"
        // and "blueocean-personalization" plugin ExtensionPoint contributions.
        JSONArray extensionData = JenkinsJSExtensions.getExtensionsData();

        Assert.assertTrue(extensionData.size() > 2);

        Iterator pluginIterator = extensionData.iterator();
        while (pluginIterator.hasNext()) {
            JSONObject plugin = (JSONObject) pluginIterator.next();
            JSONArray extensionPoints = (JSONArray) plugin.get("extensions");
            String pluginId = plugin.getString("hpiPluginId");

            Assert.assertNotNull(plugin.get("hpiPluginVer"));
            Assert.assertNotNull(extensionPoints);

            if ("blueocean-dashboard".equals(pluginId)) {
                Assert.assertEquals(7, extensionPoints.size());
                Assert.assertEquals("AdminNavLink", extensionPoints.getJSONObject(0).get("component"));
                Assert.assertEquals("jenkins.logo.top", extensionPoints.getJSONObject(0).get("extensionPoint"));
            } else if ("blueocean-personalization".equals(pluginId)) {
                Assert.assertEquals(5, extensionPoints.size());
                Assert.assertEquals("redux/FavoritesStore", extensionPoints.getJSONObject(0).get("component"));
                Assert.assertEquals("jenkins.main.stores", extensionPoints.getJSONObject(0).get("extensionPoint"));
                Assert.assertEquals("components/DashboardCards", extensionPoints.getJSONObject(1).get("component"));
                Assert.assertEquals("jenkins.pipeline.list.top", extensionPoints.getJSONObject(1).get("extensionPoint"));
                Assert.assertEquals("components/FavoritePipeline", extensionPoints.getJSONObject(2).get("component"));
                Assert.assertEquals("jenkins.pipeline.list.action", extensionPoints.getJSONObject(2).get("extensionPoint"));
                Assert.assertEquals("components/FavoritePipelineHeader", extensionPoints.getJSONObject(3).get("component"));
                Assert.assertEquals("jenkins.pipeline.detail.header.action", extensionPoints.getJSONObject(3).get("extensionPoint"));
                Assert.assertEquals("components/FavoritePipeline", extensionPoints.getJSONObject(4).get("component"));
                Assert.assertEquals("jenkins.pipeline.branches.list.action", extensionPoints.getJSONObject(4).get("extensionPoint"));
            }
        }

        // Calling JenkinsJSExtensions.getJenkinsJSExtensionData() multiple times should
        // result in the same object instance being returned because the list of plugin
        // has not changed i.e. we have a simple optimization in there where we only scan
        // the classpath if the active plugin lust has changed.
        Assert.assertEquals(
            JenkinsJSExtensions.getJenkinsJSExtensionData(),
            JenkinsJSExtensions.getJenkinsJSExtensionData()
        );
    }

    @Override
    protected String getContextPath() {
        return "blue";
    }
}
