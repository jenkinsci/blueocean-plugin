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
package io.jenkins.blueocean.blueocean_git_pipeline;

import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.util.HttpResponses;
import io.jenkins.blueocean.RootRoutable;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.DELETE;
import org.kohsuke.stapler.verb.GET;

/**
 * Provides access to any clone progress to requestors
 */
@Extension
public class RepositoryCloneProgressEndpoint implements RootRoutable {
    @Override
    public String getUrlName() {
        return "clone-progress";
    }

    @GET
    @WebMethod(name="")
    public HttpResponse getProgress(StaplerRequest req) {
        String repositoryUrl = req.getOriginalRestOfPath();
        CloneProgressMonitor progress = CloneProgressMonitor.get(repositoryUrl);
        if (progress == null) {
            return null;
        }
        return HttpResponses.okJSON(ImmutableMap.of("progress", progress.getPercentComplete()));
    }

    @DELETE
    @WebMethod(name="")
    public HttpResponse cancelClone(StaplerRequest req) {
        String repositoryUrl = req.getOriginalRestOfPath();
        CloneProgressMonitor progress = CloneProgressMonitor.get(repositoryUrl);
        if (progress != null) {
            progress.cancel();
        }
        return HttpResponses.ok();
    }
}
