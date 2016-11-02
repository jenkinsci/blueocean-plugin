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
package io.jenkins.blueocean;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BlueOceanWebURLBuilderTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void test_freestyle() throws IOException, ExecutionException, InterruptedException {
        MockFolder folder1 = jenkinsRule.createFolder("folder1");
        MockFolder folder2 = folder1.createProject(MockFolder.class, "folder two with spaces");
        FreeStyleProject freestyleProject = folder2.createProject(FreeStyleProject.class, "freestyle with spaces");
        String blueOceanURL;

        blueOceanURL = BlueOceanWebURLBuilder.toBlueOceanURL(freestyleProject);
        assertURL("blue/organizations/jenkins/folder1%2Ffolder%20two%20with%20spaces%2Ffreestyle%20with%20spaces", blueOceanURL);

        FreeStyleBuild run = freestyleProject.scheduleBuild2(0).get();
        blueOceanURL = BlueOceanWebURLBuilder.toBlueOceanURL(run);
        assertURL("blue/organizations/jenkins/folder1%2Ffolder%20two%20with%20spaces%2Ffreestyle%20with%20spaces/detail/freestyle%20with%20spaces/1", blueOceanURL);
    }

    private void assertURL(String expected, String actual) throws IOException {
        Assert.assertEquals(jenkinsRule.getURL().toString() + expected, actual);
    }
}
