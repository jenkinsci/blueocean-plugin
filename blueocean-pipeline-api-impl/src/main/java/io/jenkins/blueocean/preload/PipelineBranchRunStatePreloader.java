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
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.blueocean.commons.stapler.Export;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.factory.BlueRunFactory;
import io.jenkins.blueocean.rest.model.BlueRun;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Preload pipeline run onto the page if the requested page is a pipeline run details page for
 * a Multi Branch Project branch.
 * <p>
 * Yes, we don't need to load specific run details for npn MBP runs atm. This may change of
 * course, in which case we need to tweak this class impl.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Extension
public class PipelineBranchRunStatePreloader extends RESTFetchPreloader {

    private static final Logger LOGGER = Logger.getLogger(PipelineBranchRunStatePreloader.class.getName());

    @Override
    protected FetchData getFetchData(@NonNull BlueUrlTokenizer blueUrl) {
        //
        // See class description.
        // We're only interested in e.g. /jenkins/blue/organizations/jenkins/ATH/detail/activateFolder/1/pipeline/
        // i.e. a MBP branch run details page i.e. not interested in non MBPs
        //
        if (!blueUrl.hasPart(BlueUrlTokenizer.UrlPart.BRANCH) ||
            !blueUrl.hasPart(BlueUrlTokenizer.UrlPart.PIPELINE_RUN_DETAIL_ID)) {
            // Not interested in it
            return null;
        }

        Jenkins jenkins = Jenkins.getInstance();
        String pipelineFullName = blueUrl.getPart(BlueUrlTokenizer.UrlPart.PIPELINE);
        String branchName = blueUrl.getPart(BlueUrlTokenizer.UrlPart.BRANCH);
        String runId = blueUrl.getPart(BlueUrlTokenizer.UrlPart.PIPELINE_RUN_DETAIL_ID);
        Item pipelineJobItem = jenkins.getItemByFullName(pipelineFullName);

        if (pipelineJobItem instanceof MultiBranchProject) {
            try {
                MultiBranchProject pipelineMBP = (MultiBranchProject) pipelineJobItem;
                Job pipelineBranchJob = pipelineMBP.getItem(branchName);

                if (pipelineBranchJob != null) {
                    Run run = pipelineBranchJob.getBuild(runId);

                    if (run != null) {
                        BlueRun blueRun = BlueRunFactory.getRun(run, BluePipelineFactory.resolve(pipelineBranchJob));
                        if (blueRun != null) {
                            try {
                                return new FetchData(blueRun.getLink().getHref(), Export.toJson(blueRun));
                            } catch (IOException e) {
                                LOGGER.log(Level.FINE, String.format("Unable to preload run for pipeline '%s'. Run serialization error.", run.getUrl()), e);
                                return null;
                            }
                        } else {
                            LOGGER.log(Level.FINE, String.format("Unable to find run %s on branch named %s on pipeline named '%s'.", runId, branchName, pipelineFullName));
                            return null;
                        }
                    }
                } else {
                    LOGGER.log(Level.FINE, String.format("Unable to find branch named %s on pipeline named '%s'.", branchName, pipelineFullName));
                    return null;
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, String.format("Unable to find run from pipeline named '%s'.", pipelineFullName), e);
                return null;
            }
        } else {
            LOGGER.log(Level.FINE, String.format("Unable to find pipeline named '%s'.", pipelineFullName));
            return null;
        }

        // Don't preload any data on the page.
        return null;
    }
}
