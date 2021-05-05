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
package io.jenkins.blueocean.preload;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BlueUrlTokenizerTest {

    @Test
    public void test_MalformedURLException() {
        Assert.assertNull(BlueUrlTokenizer.parse("/a"));
    }

    @Test
    public void test() {
        BlueUrlTokenizer blueUrl;

        blueUrl = BlueUrlTokenizer.parse("/blue/pipelines/");
        Assert.assertEquals("pipelines", blueUrl.getPart(BlueUrlTokenizer.UrlPart.DASHBOARD_PIPELINES));
        Assert.assertEquals(BlueUrlTokenizer.UrlPart.DASHBOARD_PIPELINES, blueUrl.getLastPart());
        Assert.assertTrue(blueUrl.lastPartIs(BlueUrlTokenizer.UrlPart.DASHBOARD_PIPELINES));

        blueUrl = BlueUrlTokenizer.parse("/blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/activity/");
        Assert.assertFalse(blueUrl.hasPart(BlueUrlTokenizer.UrlPart.DASHBOARD_PIPELINES));
        Assert.assertTrue(blueUrl.hasPart(BlueUrlTokenizer.UrlPart.ORGANIZATION));
        Assert.assertEquals("jenkins", blueUrl.getPart(BlueUrlTokenizer.UrlPart.ORGANIZATION));
        Assert.assertEquals("f1/f3 with spaces/f3 pipeline", blueUrl.getPart(BlueUrlTokenizer.UrlPart.PIPELINE));
        Assert.assertEquals("activity", blueUrl.getPart(BlueUrlTokenizer.UrlPart.PIPELINE_TAB));
        Assert.assertEquals(BlueUrlTokenizer.UrlPart.PIPELINE_TAB, blueUrl.getLastPart());
        Assert.assertTrue(blueUrl.lastPartIs(BlueUrlTokenizer.UrlPart.PIPELINE_TAB, "activity"));

        blueUrl = BlueUrlTokenizer.parse("/blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/detail/magic-branch-X/55/pipeline");
        Assert.assertFalse(blueUrl.hasPart(BlueUrlTokenizer.UrlPart.DASHBOARD_PIPELINES));
        Assert.assertTrue(blueUrl.hasPart(BlueUrlTokenizer.UrlPart.ORGANIZATION));
        Assert.assertEquals("jenkins", blueUrl.getPart(BlueUrlTokenizer.UrlPart.ORGANIZATION));
        Assert.assertEquals("f1/f3 with spaces/f3 pipeline", blueUrl.getPart(BlueUrlTokenizer.UrlPart.PIPELINE));
        Assert.assertFalse(blueUrl.hasPart(BlueUrlTokenizer.UrlPart.PIPELINE_TAB));
        Assert.assertTrue(blueUrl.hasPart(BlueUrlTokenizer.UrlPart.PIPELINE_RUN_DETAIL));
        Assert.assertEquals("magic-branch-X", blueUrl.getPart(BlueUrlTokenizer.UrlPart.BRANCH));
        Assert.assertEquals("55", blueUrl.getPart(BlueUrlTokenizer.UrlPart.PIPELINE_RUN_DETAIL_ID));
        Assert.assertEquals("pipeline", blueUrl.getPart(BlueUrlTokenizer.UrlPart.PIPELINE_RUN_DETAIL_TAB));
        Assert.assertEquals(BlueUrlTokenizer.UrlPart.PIPELINE_RUN_DETAIL_TAB, blueUrl.getLastPart());
        Assert.assertTrue(blueUrl.lastPartIs(BlueUrlTokenizer.UrlPart.PIPELINE_RUN_DETAIL_TAB, "pipeline"));
    }

}
