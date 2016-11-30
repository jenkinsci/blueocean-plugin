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

import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.ModelObject;
import hudson.model.Run;
import io.jenkins.blueocean.rest.impl.pipeline.BranchImpl;
import io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.BluePipelineFactory;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Utility class for constructing Blue Ocean UI URLs.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Restricted(NoExternalUse.class)
public class BlueOceanWebURLBuilder {

    private BlueOceanWebURLBuilder() {
    }

    /**
     * Construct a Blue Ocean web URL for the Jenkins {@link ModelObject}
     * associated with the current Stapler request.
     *
     * @return The most appropriate Blue Ocean web URL for the current classic
     * Jenkins page. The URL to the Blue Ocean homepage is returned if a more
     * appropriate URL is not found.
     */
    public static @Nonnull String toBlueOceanURL() {
        StaplerRequest staplerRequest = Stapler.getCurrentRequest();
        List<Ancestor> list = staplerRequest.getAncestors();

        // reverse iterate on the list of ancestors, looking for a
        // Blue Ocean page we can link onto.
        for (int i = list.size() - 1; i >= 0; i--) {
            Ancestor ancestor = list.get(i);
            Object object = ancestor.getObject();

            if (object instanceof ModelObject) {
                String blueUrl = toBlueOceanURL((ModelObject) object);
                if (blueUrl != null) {
                    return blueUrl;
                }
            }
        }

        // Otherwise just return Blue Ocean home.
        return getBlueHome();
    }

    /**
     * Construct a Blue Ocean web URL for the supplied Jenkins {@link ModelObject}.
     *
     * @param classicModelObject The class Jenkins Model Object from which to construct
     *                           a Blue Ocean URL.
     * @return The most appropriate Blue Ocean web URL for the supplied Jenkins
     * {@link ModelObject}, or {@code null} if no URL can be constructed.
     */
    public static @CheckForNull String toBlueOceanURL(@Nonnull ModelObject classicModelObject) {
        if (classicModelObject instanceof Job) {
            BlueOceanModelMapping pipelineModelMapping = getPipelineModelMapping((Job) classicModelObject);
            if (pipelineModelMapping.blueModelObject instanceof BlueMultiBranchPipeline) {
                return pipelineModelMapping.blueUiUrl + "/branches";
            } else {
                return pipelineModelMapping.blueUiUrl;
            }
        } else if (classicModelObject instanceof Run) {
            Run run = (Run) classicModelObject;
            Job job = run.getParent();
            BlueOceanModelMapping pipelineModelMapping = getPipelineModelMapping(job);
            return pipelineModelMapping.blueUiUrl + "/detail/" + encodeURIComponent(job.getName()) + "/" + encodeURIComponent(run.getId());
        } else if (classicModelObject instanceof Item) {
            Resource blueResource = BluePipelineFactory.resolve((Item) classicModelObject);
            if (blueResource != null) {
                if (blueResource instanceof BlueMultiBranchPipeline) {
                    return getOrgPrefix() + "/" + encodeURIComponent(((BluePipeline) blueResource).getFullName()) + "/branches";
                } else if (blueResource instanceof BluePipeline) {
                    return getOrgPrefix() + "/" + encodeURIComponent(((BluePipeline) blueResource).getFullName());
                }
            }
        }

        return null;
    }

    private static String getOrgPrefix() {
        return getBlueHome() + "/organizations/" + OrganizationImpl.INSTANCE.getName();
    }

    private static String getBlueHome() {
        String rootUrl = Jenkins.getInstance().getRootUrl();

        if (rootUrl == null) {
            throw new IllegalStateException("Unable to determine Jenkins root URL.");
        }

        if (rootUrl.endsWith("/")) {
            rootUrl = rootUrl.substring(0, rootUrl.length() - 1);
        }

        return rootUrl + "/blue";
    }

    private static BlueOceanModelMapping getPipelineModelMapping(Job job) {
        BluePipeline blueResource = (BluePipeline) BluePipelineFactory.resolve(job);

        if (blueResource instanceof BranchImpl) { // No abstract "Branch" type?
            ItemGroup multibranchJob = job.getParent();
            BluePipeline multibranchJobResource = (BluePipeline) BluePipelineFactory.resolve((Item) multibranchJob);

            return new BlueOceanModelMapping(
                multibranchJob,
                multibranchJobResource,
                getOrgPrefix() + "/" + encodeURIComponent(multibranchJobResource.getFullName())
            );
        } else {
            return new BlueOceanModelMapping(
                job,
                blueResource,
                getOrgPrefix() + "/" + encodeURIComponent(blueResource.getFullName())
            );
        }
    }

    static String encodeURIComponent(String string) {
        try {
            // The Java URLEncoder encodes spaces as "+", while the javascript
            // encodeURIComponent function encodes them as "%20". We need to make them
            // consistent with how it's done in encodeURIComponent, so replace the
            // "+" with "%20".
            return URLEncoder.encode(string, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unexpected UTF-8 encoding error.", e);
        }
    }

    private static class BlueOceanModelMapping {

        private Object classJenkinsModelObject;
        private Resource blueModelObject;
        private String blueUiUrl;

        public BlueOceanModelMapping(Object classJenkinsModelObject, Resource blueModelObject, String blueUiUrl) {
            this.classJenkinsModelObject = classJenkinsModelObject;
            this.blueModelObject = blueModelObject;
            this.blueUiUrl = blueUiUrl;
        }
    }
}
