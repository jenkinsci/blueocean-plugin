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
import io.jenkins.blueocean.rest.model.BlueChangeSetEntry;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.rest.pageable.PagedResponse;
import io.jenkins.blueocean.service.embedded.rest.ChangeSetResource;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class BlueRunChangesetPreloader extends RESTFetchPreloader {

    private static final Logger LOGGER = Logger.getLogger(PipelineActivityStatePreloader.class.getName());

    @Override
    protected FetchData getFetchData(@Nonnull BlueUrlTokenizer blueUrl) {

        if (!blueUrl.lastPartIs(BlueUrlTokenizer.UrlPart.PIPELINE_RUN_DETAIL_TAB, "changes")) {
            // Not interested in it
            return null;
        }

        BluePipeline pipeline = getPipeline(blueUrl);

        if (pipeline == null) {
            // Not interested in it
            return null;
        }

        // It's a pipeline page. Let's prefetch the pipeline activity and add them to the page,
        // saving the frontend the overhead of requesting them.

        Container<BlueRun> activitiesContainer = pipeline.getRuns();
        if(activitiesContainer==null){
            return null;
        }
        BlueRun run = activitiesContainer.get(blueUrl.getPart(BlueUrlTokenizer.UrlPart.PIPELINE_RUN_DETAIL_ID));
        Container<BlueChangeSetEntry> containerChangeSets = run.getChangeSet();
        return getFetchData(containerChangeSets);
    }

    public FetchData getFetchData(Container<BlueChangeSetEntry> containerChangeSets) {
        try {
            JSONArray changeSetEntries = new JSONArray();

            for (BlueChangeSetEntry changeSetEntry: containerChangeSets) {
                changeSetEntries.add(JSONObject.fromObject(Export.toJson(changeSetEntry)));
            };

            // organizations/jenkins/pipelines/changes/runs/12/changeSet/?start=0&limit=101
            return new FetchData(
                    containerChangeSets.getLink().getHref() + "?start=0&limit=101",
                    changeSetEntries.toString()
            );
        } catch (IOException e) {
            LOGGER.log(Level.FINE, String.format("Unable to preload changelog data '%s'. Failed to convert to Blue Ocean Resource.", containerChangeSets.getLink().getHref()));
            return null;
        }
    }

    private BluePipeline getPipeline(BlueUrlTokenizer blueUrl) {
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) { return null; }
        String pipelineFullName = blueUrl.getPart(BlueUrlTokenizer.UrlPart.PIPELINE);

        try {
            Item pipelineJob = jenkins.getItemByFullName(pipelineFullName);
            return (BluePipeline) BluePipelineFactory.resolve(pipelineJob);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, String.format("Unable to find Job named '%s'.", pipelineFullName), e);
            return null;
        }
    }
}
