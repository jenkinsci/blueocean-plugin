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

import hudson.util.PluginServletFilter;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Resource cache-control filter.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Restricted(NoExternalUse.class)
public final class ResourceCacheControl implements Filter {

    private static ResourceCacheControl INSTANCE;

    private final List<String> resourcePrefixes = new ArrayList<>();

    ResourceCacheControl() {
    }

    String addPath(String path) {
        // Make sure the paths always start and end with a slash.
        // This simplifies later comparison with the request path.
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path =  path + "/";
        }
        resourcePrefixes.add(path);

        return path;
    }

    public static synchronized void install() {
        if (INSTANCE != null) {
            return;
        }
        INSTANCE = new ResourceCacheControl();

        // Don't add the filter if we are running with hpi:run. Otherwise, people need
        // to do a hard reload (bypassing the cache), which may confuse people during dev.
        if (!Boolean.getBoolean("hudson.hpi.run")) {
            try {
                // Add paths to resources that we want to set the
                // cache-control header.
                INSTANCE.addPath(Jenkins.RESOURCE_PATH); // "/static/VERSION" resources - e.g. JDL assets (fonts etc)
                INSTANCE.addPath(Jenkins.getInstance().getAdjuncts("").rootURL);

                PluginServletFilter.addFilter(INSTANCE);
            } catch (Exception e) {
                throw new IllegalStateException("Unexpected Exception installing Blue Web Resource Adjunct cache control filter.", e);
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Check the request path and set the cache-control header if we find
        // it matches what we're looking for.
        if (request instanceof HttpServletRequest) {
            if (isCacheableResourceRequest((HttpServletRequest)request)) {
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;

                //
                // Set the expiry to one year.
                //
                // Note that this does NOT mean that the browser will never send a request
                // for these resources. If you click reload in the browser (def in Chrome) it will
                // send an If-Modified-Since request to the server (at a minimum), which means you at
                // least have the request overhead even if it results in a 304 response. Setting the
                // Cache-Control header helps for normal browsing (clicking on links, bookmarks etc),
                // in which case the local cache is fully used (no If-Modified-Since requests for
                // non-stale resources).
                //
                httpServletResponse.setHeader("Cache-Control", "public, max-age=31536000");
                response = new HttpServletResponseWrapper(httpServletResponse) {
                    @Override
                    public void setHeader(String name, String value) {
                        // Block the setting of the legacy HTTP/1.0 "Expires" header.
                        // Note that, strictly speaking, this should not be required because
                        // the HTTP spec dictates that the Cache-Control header takes priority.
                        // Lets eliminate it anyway in case a browser/intermediary doesn't comply.
                        if (!name.equalsIgnoreCase("Expires")) {
                            super.setHeader(name, value);
                        }
                    }
                };
            }
        }
        // continue to execute the filer chain as normal
        chain.doFilter(request, response);
    }

    boolean isCacheableResourceRequest(HttpServletRequest request) {
        String requestPath = request.getPathInfo();
        if (requestPath == null) {
            return false;
        }
        for (String resourcePrefix : resourcePrefixes) {
            if (requestPath.startsWith(resourcePrefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
    }
}
