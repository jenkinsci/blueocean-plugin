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

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import io.jenkins.blueocean.BlueOceanWebURLBuilder;
import io.jenkins.blueocean.service.embedded.BaseTest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StatePreloaderTest extends BaseTest {

    @Test
    public void test() throws IOException, ExecutionException, InterruptedException, SAXException {
        // Create a project and run a build on it.
        FreeStyleProject freestyleProject = j.createProject(FreeStyleProject.class, "freestyle");
        FreeStyleBuild run = freestyleProject.scheduleBuild2(0).get();
        j.waitForCompletion(run);

        // Lets request the activity page for that project. The page should
        // contain some prefetched javascript for the pipeline
        // details + the runs on the page
        String projectBlueUrl = BlueOceanWebURLBuilder.toBlueOceanURL(freestyleProject);
        Document doc = Jsoup.connect(projectBlueUrl + "/activity/").get();
        String script = doc.select("head script").toString();

        Assert.assertTrue(script.contains(String.format("setState('prefetchdata.%s',", PipelineStatePreloader.class.getSimpleName())));
        Assert.assertTrue(script.contains(String.format("setState('prefetchdata.%s',", PipelineActivityStatePreloader.class.getSimpleName())));
        Assert.assertTrue(script.contains("\"restUrl\":\"/blue/rest/organizations/jenkins/pipelines/freestyle/activities/?start=0&limit=26\""));
    }
}
