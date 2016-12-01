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
package io.jenkins.blueocean.i18n;

import io.jenkins.blueocean.service.embedded.BaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class BlueI18nTest extends BaseTest {

    @Before
    public void waitForPluginLoaded() throws InterruptedException {
        // We've seen situations on the CI server where the plugin
        // was not yet installed, causing the tests to fail. This piece of
        // code basically blocks the test running untill everything is there.
        long start = System.currentTimeMillis();
        while (BlueI18n.getPlugin("blueocean-dashboard") == null) {
            if (System.currentTimeMillis() > start + 10000) {
                break;
            }
            Thread.sleep(100);
        }
    }

    @Test
    public void test_200_response_locale_match() {
        String dashboardVersion = BlueI18n.getPlugin("blueocean-dashboard").getVersion();

        Map<String, Object> response1 = get("/blueocean-i18n/blueocean-dashboard/" + dashboardVersion + "/jenkins.plugins.blueocean.dashboard.Messages/de", HttpServletResponse.SC_OK, Map.class);

        Assert.assertEquals("ok", response1.get("status"));
        Assert.assertTrue(((Map<String, Object>)response1.get("data")).containsKey("common.date.duration.format"));
    }

    @Test
    public void test_200_response_locale_fallback() {
        String dashboardVersion = BlueI18n.getPlugin("blueocean-dashboard").getVersion();

        // Make sure we fallback to the base resource bundle def if an unknown locale is requested.
        // Of course, we will need to modify this test if translations for the test languages are added.
        get("/blueocean-i18n/blueocean-dashboard/" + dashboardVersion + "/jenkins.plugins.blueocean.dashboard.Messages/en", HttpServletResponse.SC_OK, Map.class);
        get("/blueocean-i18n/blueocean-dashboard/" + dashboardVersion + "/jenkins.plugins.blueocean.dashboard.Messages/en_US", HttpServletResponse.SC_OK, Map.class);
        get("/blueocean-i18n/blueocean-dashboard/" + dashboardVersion + "/jenkins.plugins.blueocean.dashboard.Messages/jp", HttpServletResponse.SC_OK, Map.class);
    }

    @Test
    public void test_200_response_caching() {
        String dashboardVersion = BlueI18n.getPlugin("blueocean-dashboard").getVersion();

        Map<String, Object> response1 = get("/blueocean-i18n/blueocean-dashboard/" + dashboardVersion + "/jenkins.plugins.blueocean.dashboard.Messages/de", HttpServletResponse.SC_OK, Map.class);
        Map<String, Object> response2 = get("/blueocean-i18n/blueocean-dashboard/" + dashboardVersion + "/jenkins.plugins.blueocean.dashboard.Messages/de", HttpServletResponse.SC_OK, Map.class);

        // Make sure the second comes from the cache. The "cache-timestamp" field
        // will be different if it didn't, resulting in an assert failure.
        Assert.assertEquals(response1, response2);
    }

    @Test
    public void test_404_response_unknown_bundle() {
        String dashboardVersion = BlueI18n.getPlugin("blueocean-dashboard").getVersion();

        Map<String, Object> response1 = get("/blueocean-i18n/blueocean-dashboard/" + dashboardVersion + "/jenkins.plugins.blueocean.dashboard.XXXUnknown/en", HttpServletResponse.SC_NOT_FOUND, Map.class);

        Assert.assertEquals("error", response1.get("status"));
        Assert.assertEquals("Unknown plugin or resource bundle: blueocean-dashboard/" + dashboardVersion + "/jenkins.plugins.blueocean.dashboard.XXXUnknown/en", response1.get("message"));
    }

    @Test
    public void test_404_response_unknown_plugin() {
        Map<String, Object> response1 = get("/blueocean-i18n/blueocean-xxxblah/1.0.0/jenkins.plugins.blueocean.dashboard.Messages/en", HttpServletResponse.SC_NOT_FOUND, Map.class);

        Assert.assertEquals("error", response1.get("status"));
        Assert.assertEquals("Unknown plugin or resource bundle: blueocean-xxxblah/1.0.0/jenkins.plugins.blueocean.dashboard.Messages/en", response1.get("message"));
    }

    @Test
    public void test_browser_cacheable() {
        String version = BlueI18n.getPlugin("cloudbees-folder").getVersion();
        BlueI18n.BundleParams bundleParams = BlueI18n.getBundleParameters(String.format("cloudbees-folder/%s/pluginx.bundle", version));

        // Should be cacheable because the installed version matches the requested version + the
        // version is a release version (not a SNAPSHOT version).
        Assert.assertTrue(bundleParams.isBrowserCacheable());
    }

    @Test
    public void test_not_browser_cacheable() {
        BlueI18n.BundleParams bundleParams;

        // Should NOT be cacheable because the installed version doesn't matches the requested version
        bundleParams = BlueI18n.getBundleParameters("cloudbees-folder/0.1111/pluginx.bundle");
        Assert.assertFalse(bundleParams.isBrowserCacheable());

        // Should NOT be cacheable because the requested version was a SNAPSHOT
        bundleParams = BlueI18n.getBundleParameters("cloudbees-folder/0.1-SNAPSHOT/pluginx.bundle");
        Assert.assertFalse(bundleParams.isBrowserCacheable());
    }

    @Override
    protected String getContextPath() {
        return "";
    }
}
