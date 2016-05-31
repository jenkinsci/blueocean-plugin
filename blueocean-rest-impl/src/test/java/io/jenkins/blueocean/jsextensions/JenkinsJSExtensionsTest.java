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

import io.jenkins.blueocean.service.embedded.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JenkinsJSExtensionsTest extends BaseTest{

    @Test
    public void test() {
        // FIXME: This test relies on configuration in a separate project
        // Simple test of the rest endpoint. It should find the "blueocean-dashboard"
        // plugin ExtensionPoint contributions.
        List<Map> extensions = get("/javaScriptExtensionInfo", List.class);

        Assert.assertEquals(1, extensions.size());
        Assert.assertEquals("blueocean-dashboard", extensions.get(0).get("hpiPluginId"));

        List<Map> ext = (List<Map>) extensions.get(0).get("extensions");

        Assert.assertEquals(4, ext.size());
        Assert.assertEquals("AdminNavLink", ext.get(0).get("component"));
        Assert.assertEquals("jenkins.logo.top", ext.get(0).get("extensionPoint"));

        // Calling JenkinsJSExtensions.getJenkinsJSExtensionData() multiple times should
        // result in the same object instance being returned because the list of plugin
        // has not changed i.e. we have a simple optimization in there where we only scan
        // the classpath if the active plugin lust has changed.
        Assert.assertArrayEquals(
            JenkinsJSExtensions.INSTANCE.getJenkinsJSExtensionData(),
            JenkinsJSExtensions.INSTANCE.getJenkinsJSExtensionData()
        );
    }

    @Override
    protected String getContextPath() {
        return "blue";
    }
}
