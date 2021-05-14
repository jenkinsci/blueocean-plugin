/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Item;
import io.jenkins.blueocean.commons.stapler.Export;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.model.BluePipeline;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Preload pipeline onto the page if the requested page is a pipeline page.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Extension
public class PipelineStatePreloader extends RESTFetchPreloader {

    private static final Logger LOGGER = Logger.getLogger(PipelineStatePreloader.class.getName());

    @Override
    protected FetchData getFetchData(@NonNull BlueUrlTokenizer blueUrl) {
        // e.g. /blue/organizations/jenkins/Pipeline (or a url on that)
        if (!blueUrl.hasPart(BlueUrlTokenizer.UrlPart.PIPELINE)) {
            // Not interested in it
            return null;
        }

        Jenkins jenkins = Jenkins.getInstance();
        String pipelineFullName = blueUrl.getPart(BlueUrlTokenizer.UrlPart.PIPELINE);

        try {
            Item pipelineJobItem = jenkins.getItemByFullName(pipelineFullName);

            if (pipelineJobItem != null) {
                BluePipeline bluePipeline = (BluePipeline) BluePipelineFactory.resolve(pipelineJobItem);
                if (bluePipeline != null) {
                    try {
                        return new FetchData(bluePipeline.getLink().getHref(), Export.toJson(bluePipeline));
                    } catch (IOException e) {
                        LOGGER.log(Level.FINE, String.format("Unable to preload pipeline '%s'. Serialization error.", pipelineJobItem.getUrl()), e);
                        return null;
                    }
                } else {
                    LOGGER.log(Level.FINE, String.format("Unable to preload pipeline '%s'. Failed to convert to Blue Ocean Resource.", pipelineJobItem.getUrl()));
                    return null;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, String.format("Unable to find pipeline named '%s'.", pipelineFullName), e);
            return null;
        }

        // Don't preload any data on the page.
        return null;
    }
}
