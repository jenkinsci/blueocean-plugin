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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BlueI18nTest {

    @Test
    public void test_getBundleParameters_invalid_url() {
        Assert.assertNull(BlueI18n.getBundleParameters("pluginx/1.0.0"));
        Assert.assertNull(BlueI18n.getBundleParameters("/pluginx/1.0.0"));
    }

    @Test
    public void test_getBundleParameters_valid_url() {
        BlueI18n.BundleParams bundleParameters = BlueI18n.getBundleParameters("pluginx/1.0.0/pluginx.bundle");
        Assert.assertNotNull(bundleParameters);
        Assert.assertEquals("pluginx", bundleParameters.pluginName);
        Assert.assertEquals("1.0.0", bundleParameters.pluginVersion);
        Assert.assertEquals("pluginx.bundle", bundleParameters.bundleName);
        Assert.assertNull(bundleParameters.language);
        Assert.assertNull(bundleParameters.country);
        Assert.assertNull(bundleParameters.variant);
        Assert.assertNull(bundleParameters.getLocale());

        bundleParameters = BlueI18n.getBundleParameters("pluginx/1.0.0/pluginx.bundle/en");
        Assert.assertNotNull(bundleParameters);
        Assert.assertEquals("pluginx", bundleParameters.pluginName);
        Assert.assertEquals("1.0.0", bundleParameters.pluginVersion);
        Assert.assertEquals("pluginx.bundle", bundleParameters.bundleName);
        Assert.assertEquals("en", bundleParameters.language);
        Assert.assertNull(bundleParameters.country);
        Assert.assertNull(bundleParameters.variant);
        Assert.assertNotNull(bundleParameters.getLocale());
        Assert.assertEquals("en", bundleParameters.getLocale().toString());
    }

    @Test
    public void test_getBundleParameters_locale() {
        BlueI18n.BundleParams bundleParameters;

        bundleParameters = BlueI18n.getBundleParameters("pluginx/1.0.0/pluginx.bundle/ja_JP_JP");
        Assert.assertEquals("ja_JP_JP_#u-ca-japanese", bundleParameters.getLocale().toString());
        bundleParameters = BlueI18n.getBundleParameters("pluginx/1.0.0/pluginx.bundle/ja-JP-JP");
        Assert.assertEquals("ja_JP_JP_#u-ca-japanese", bundleParameters.getLocale().toString());

        bundleParameters = BlueI18n.getBundleParameters("pluginx/1.0.0/pluginx.bundle/ja_JP");
        Assert.assertEquals("ja_JP", bundleParameters.getLocale().toString());
        bundleParameters = BlueI18n.getBundleParameters("pluginx/1.0.0/pluginx.bundle/ja-JP");
        Assert.assertEquals("ja_JP", bundleParameters.getLocale().toString());

        bundleParameters = BlueI18n.getBundleParameters("pluginx/1.0.0/pluginx.bundle/ja");
        Assert.assertEquals("ja", bundleParameters.getLocale().toString());
        bundleParameters = BlueI18n.getBundleParameters("pluginx/1.0.0/pluginx.bundle/ja");
        Assert.assertEquals("ja", bundleParameters.getLocale().toString());
    }

    @Test
    public void test_getBundleParameters_isReleaseVersion() {
        BlueI18n.BundleParams bundleParameters;

        bundleParameters = BlueI18n.getBundleParameters("pluginx/1.0/pluginx.bundle");
        Assert.assertTrue(bundleParameters.isReleaseVersion());
        bundleParameters = BlueI18n.getBundleParameters("pluginx/1.0.0/pluginx.bundle");
        Assert.assertTrue(bundleParameters.isReleaseVersion());
        bundleParameters = BlueI18n.getBundleParameters("pluginx/1.0-SNAPSHOT/pluginx.bundle");
        Assert.assertFalse(bundleParameters.isReleaseVersion());
        bundleParameters = BlueI18n.getBundleParameters("pluginx/1.0.0-SNAPSHOT/pluginx.bundle"); //
        Assert.assertFalse(bundleParameters.isReleaseVersion());
        bundleParameters = BlueI18n.getBundleParameters("pluginx/1/pluginx.bundle"); // must be at least 3 chars long
        Assert.assertFalse(bundleParameters.isReleaseVersion());
    }

    @Test
    public void test_getBundleParameters_version_with_slashes() {
        BlueI18n.BundleParams bundleParameters;

        bundleParameters = BlueI18n.getBundleParameters("pluginx/1.0.0-SNAPSHOT%20(something%2Felse)/pluginx.bundle");
        Assert.assertFalse(bundleParameters.isReleaseVersion());
        Assert.assertEquals("1.0.0-SNAPSHOT (something/else)", bundleParameters.pluginVersion);
    }

    @Test
    public void test_BundleParams_equals() {
        Assert.assertEquals(BlueI18n.getBundleParameters("pluginx/1.0/pluginx.bundle"), BlueI18n.getBundleParameters("pluginx/1.0/pluginx.bundle"));
        Assert.assertEquals(BlueI18n.getBundleParameters("pluginx/1.0/pluginx.bundle/en"), BlueI18n.getBundleParameters("pluginx/1.0/pluginx.bundle/en"));
        Assert.assertNotEquals(BlueI18n.getBundleParameters("pluginx/1.0/pluginx.bundle/en"), BlueI18n.getBundleParameters("pluginx/1.0/pluginx.bundle/"));
        Assert.assertNotEquals(BlueI18n.getBundleParameters("pluginx/1.0/pluginx.bundle/en"), BlueI18n.getBundleParameters("pluginx/1.0/pluginx.bundle/en_EN"));
    }
}
