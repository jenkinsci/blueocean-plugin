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

import hudson.Extension;
import hudson.model.Item;
import io.jenkins.blueocean.commons.stapler.Export;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Resource;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Preload pipeline activity onto the page if the requested page is a pipeline activity page.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Extension
public class PipelineActivityStatePreloader extends RESTFetchPreloader {

    private static final Logger LOGGER = Logger.getLogger(PipelineActivityStatePreloader.class.getName());

    private static final int DEFAULT_LIMIT = 26;

    @Override
    protected FetchData getFetchData(@Nonnull BlueUrlTokenizer blueUrl) {
        BluePipeline pipeline = getPipeline(blueUrl);

        if (pipeline != null) {
            // It's a pipeline page. Let's prefetch the pipeline activity and add them to the page,
            // saving the frontend the overhead of requesting them.

            Container<BlueRun> activitiesContainer = pipeline.getRuns();
            if(activitiesContainer==null){
                return null;
            }
            Iterator<BlueRun> activitiesIterator = activitiesContainer.iterator(0, DEFAULT_LIMIT);
            JSONArray activities = new JSONArray();

            while(activitiesIterator.hasNext()) {
                Resource blueActivity = activitiesIterator.next();
                try {
                    activities.add(JSONObject.fromObject(Export.toJson(blueActivity)));
                } catch (IOException e) {
                    LOGGER.log(Level.FINE, String.format("Unable to preload runs for Job '%s'. Activity serialization error.", pipeline.getFullName()), e);
                    return null;
                }
            }

            return new FetchData(
                activitiesContainer.getLink().getHref() + "?start=0&limit=" + DEFAULT_LIMIT,
                activities.toString());
        }

        // Don't preload any data on the page.
        return null;
    }

    private BluePipeline getPipeline(BlueUrlTokenizer blueUrl) {
        if (addPipelineRuns(blueUrl)) {
            Jenkins jenkins = Jenkins.getInstance();
            String pipelineFullName = blueUrl.getPart(BlueUrlTokenizer.UrlPart.PIPELINE);

            try {
                Item pipelineJob = jenkins.getItemByFullName(pipelineFullName);
                return (BluePipeline) BluePipelineFactory.resolve(pipelineJob);
            } catch (Exception e) {
                LOGGER.log(Level.FINE, String.format("Unable to find Job named '%s'.", pipelineFullName), e);
                return null;
            }
        }

        return null;
    }

    private boolean addPipelineRuns(@Nonnull BlueUrlTokenizer blueUrl) {
        if (blueUrl.lastPartIs(BlueUrlTokenizer.UrlPart.PIPELINE)) {
            // e.g. /blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/
            return true;
        } else if (blueUrl.lastPartIs(BlueUrlTokenizer.UrlPart.PIPELINE_TAB, "activity")) {
            // e.g. /blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/activity/
            return true;
        }

        return false;
    }
}
