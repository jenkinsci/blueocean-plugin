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

import com.jayway.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JenkinsJSExtensionsTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void before() {
        RestAssured.baseURI = j.jenkins.getRootUrl()+"blue";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void test() {
        // Simple test of the rest endpoint. It should find the "blueocean-admin"
        // plugin ExtensionPoint contributions.
        RestAssured.given().log().all().get("/javaScriptExtensionInfo")
            .then().log().all()
            .statusCode(200)
            .body("size()", Matchers.equalTo(1))
            .body("[0].hpiPluginId", Matchers.equalTo("blueocean-admin"))
            .body("[0].extensions[0].component", Matchers.equalTo("AdminNavLink"))
            .body("[0].extensions[0].extensionPoint", Matchers.equalTo("jenkins.topNavigation.menu"))
        ;

        // Calling JenkinsJSExtensions.getJenkinsJSExtensionData() multiple times should
        // result in the same object instance being returned because the list of plugin
        // has not changed i.e. we have a simple optimization in there where we only scan
        // the classpath if the active plugin lust has changed.
        Assert.assertArrayEquals(
            JenkinsJSExtensions.INSTANCE.getJenkinsJSExtensionData(),
            JenkinsJSExtensions.INSTANCE.getJenkinsJSExtensionData()
        );
    }
}
