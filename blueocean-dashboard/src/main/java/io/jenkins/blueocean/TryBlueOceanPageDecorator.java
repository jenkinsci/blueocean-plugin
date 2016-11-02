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

import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.PageDecorator;
import hudson.model.Run;
import io.jenkins.blueocean.rest.impl.pipeline.BranchImpl;
import io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.BluePipelineFactory;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Stapler page decorator for decorating classic Jenkins pages with visual
 * prompts to the user that will hopefully entice/remind them into giving
 * Blue ocean a try.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Extension
public class TryBlueOceanPageDecorator extends PageDecorator {

    public String getBlueOceanURL() throws IOException {
        StaplerRequest staplerRequest = Stapler.getCurrentRequest();
        List<Ancestor> list = staplerRequest.getAncestors();

        // reverse iterate on the list of ancestors, looking for a
        // Blue Ocean page we can link onto.
        for (int i = list.size() - 1; i >= 0; i--) {
            Ancestor ancestor = list.get(i);
            Object object = ancestor.getObject();

            if (object instanceof Job) {
                BlueOceanModelMapping pipelineModelMapping = getPipelineModelMapping((Job) object);
                if (pipelineModelMapping.blueModelObject instanceof BlueMultiBranchPipeline) {
                    return pipelineModelMapping.blueUiUrl + "/branches";
                } else {
                    return pipelineModelMapping.blueUiUrl;
                }
            } else if (object instanceof Run) {
                Run run = (Run) object;
                Job job = run.getParent();
                BlueOceanModelMapping pipelineModelMapping = getPipelineModelMapping(job);
                return pipelineModelMapping.blueUiUrl + "/detail/" + urlEncode(job.getName()) + "/" + urlEncode(run.getId()); // Or is it the run number?
            } else if (object instanceof Item) {
                Resource blueResource = BluePipelineFactory.resolve((Item) object);
                if (blueResource != null) {
                    if (blueResource instanceof BlueMultiBranchPipeline) {
                        return getOrgPrefix() + "/" + urlEncode(((BluePipeline)blueResource).getFullName()) + "/branches";
                    } else if (blueResource instanceof BluePipeline) {
                        return getOrgPrefix() + "/" + ((BluePipeline)blueResource).getFullName();
                    }
                }
            }
        }

        // Otherwise just return Blue Ocean home.
        return getBlueHome();
    }

    private BlueOceanModelMapping getPipelineModelMapping(Job job) throws UnsupportedEncodingException {
        BluePipeline blueResource = (BluePipeline) BluePipelineFactory.resolve(job);

        if (blueResource instanceof BranchImpl) { // No abstract "Branch" type?
            ItemGroup multibranchJob = job.getParent();
            BluePipeline multibranchJobResource = (BluePipeline) BluePipelineFactory.resolve((Item) multibranchJob);

            // TODO verify url encoding
            // fullName is already escaped, but it sems like we need to double escape it for
            // multibranch urls.
            return new BlueOceanModelMapping(
                multibranchJob,
                multibranchJobResource,
                getOrgPrefix() + "/" + urlEncode(multibranchJobResource.getFullName())
            );
        } else {
            // TODO verify url encoding
            // fullName is already escaped, but I don't think we double encode for
            // non multibranch (see above).
            return new BlueOceanModelMapping(
                job,
                blueResource,
                getOrgPrefix() + "/" + blueResource.getFullName()
            );
        }
    }

    private String getOrgPrefix() throws UnsupportedEncodingException {
        return getBlueHome() + "/organizations/" + urlEncode(OrganizationImpl.INSTANCE.getName());
    }

    private String getBlueHome() {
        String rootUrl = Jenkins.getInstance().getRootUrl();

        if (rootUrl.endsWith("/")) {
            rootUrl = rootUrl.substring(0, rootUrl.length() - 1);
        }

        return rootUrl + "/blue";
    }

    private class BlueOceanModelMapping {

        private Object classJenkinsModelObject;
        private Resource blueModelObject;
        private String blueUiUrl;

        public BlueOceanModelMapping(Object classJenkinsModelObject, Resource blueModelObject, String blueUiUrl) {
            this.classJenkinsModelObject = classJenkinsModelObject;
            this.blueModelObject = blueModelObject;
            this.blueUiUrl = blueUiUrl;
        }
    }

    private String urlEncode(String string) throws UnsupportedEncodingException {
        return URLEncoder.encode(string, "UTF-8");
    }
}
