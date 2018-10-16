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

import hudson.model.Run;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * General purpose Blue Ocean UI URL parser.
 * <p>
 * This class performs a "best effort" attempt to parse a URL as a Blue Ocean
 * URL, extracting what it thinks are the relevant "parts" and making available via the
 * {@link #getPart(UrlPart)} and {@link #hasPart(UrlPart)} functions.
 * <p>
 * See TBD comment on {@link UrlPart}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Restricted(NoExternalUse.class) // Internal use only for now because there's a fair chance we'll change how it works. See TBD comment on UrlPart.
public class BlueUrlTokenizer {

    private static final Set<String> PIPELINE_TABS =
        new LinkedHashSet<>(Arrays.asList("activity", "branches", "pr"));

    private static final Set<String> PIPELINE_RUN_DETAIL_TABS =
        new LinkedHashSet<>(Arrays.asList("pipeline", "changes", "tests", "artifacts"));

    private Map<UrlPart, String> urlParts = new LinkedHashMap<>();
    private UrlPart lastPart;

    /**
     * Enum of URL "parts".
     * <p>
     * Use {@link #getPart(UrlPart)} to get a specific URL "part",
     * or call {@link #hasPart(UrlPart)} to check for it's existence.
     * <p>
     * *** TBD: decide whether to stick with this model, or to switch to more of a straight getters/setters style on the {@link BlueUrlTokenizer} instance.
     * Reason for trying this approach ("parts" enum) is that I (TF) think the straight properties style with getters/setters
     * would get messy as we add support for parsing more URL paths/parts i.e. a getters/setters API explosion.
     * That said ... not sure I love this approach either, hence marked BlueoceanUrl as @Restricted(NoExternalUse.class). Let's suck it
     * and see for a bit and change if it sucks :)
     */
    public enum UrlPart {
        /**
         * Main blue ocean pipelines dashboard.
         * i.e. /blue/pipelines/
         */
        DASHBOARD_PIPELINES,
        /**
         * A URL pointing at a page associated with an "organization" resource.
         * e.g. /blue/organizations/jenkins/...
         * <p>
         * Call Use {@link #getPart(UrlPart)} to get the organization name.
         */
        ORGANIZATION,
        /**
         * A URL pointing at a pipeline.
         * e.g. /blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/...
         * <p>
         * Call Use {@link #getPart(UrlPart)} to get the pipeline name. Note that the URL
         * may have additional parts (e.g. {@link UrlPart#PIPELINE_TAB} or {@link UrlPart#PIPELINE_RUN_DETAIL}).
         */
        PIPELINE,
        /**
         * A URL pointing at a pipeline tab.
         * e.g. /blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/activity
         * <p>
         * Call Use {@link #getPart(UrlPart)} to get the tab name.
         */
        PIPELINE_TAB,
        /**
         * A URL pointing at a pipeline Run Details.
         * e.g. // e.g. /blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/detail/...
         * <p>
         * See {@link #BRANCH} for sub-component of this URL.
         */
        PIPELINE_RUN_DETAIL,
        /**
         * A URL pointing at a pipeline Run Details for a specific branch.
         * e.g. // e.g. /blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/detail/magic-branch-X/...
         * <p>
         * See {@link #PIPELINE_RUN_DETAIL_ID} for sub-component of this URL.
         * <p>
         * Call Use {@link #getPart(UrlPart)} to get the branch name.
         */
        BRANCH,
        /**
         * A URL pointing at a pipeline Run Details for a specific run of a specific branch.
         * e.g. // e.g. /blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/detail/magic-branch-X/55/...
         * <p>
         * See {@link #PIPELINE_RUN_DETAIL_ID} for sub-component of this URL.
         * <p>
         * Call Use {@link #getPart(UrlPart)} to get the {@link Run} ID.
         */
        PIPELINE_RUN_DETAIL_ID,
        /**
         * A URL pointing at one of the tabs on a pipeline Run Details for a specific run of a specific branch.
         * e.g. // e.g. /blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/detail/magic-branch-X/55/artifacts
         * <p>
         * Call Use {@link #getPart(UrlPart)} to get the tab name.
         */
        PIPELINE_RUN_DETAIL_TAB,
    }

    private BlueUrlTokenizer() {
    }

    /**
     * Parse the {@link Stapler#getCurrentRequest() current Stapler request} and return a {@link BlueUrlTokenizer} instance
     * iff the URL is a Blue Ocean UI URL.
     *
     * @return A {@link BlueUrlTokenizer} instance iff the URL is a Blue Ocean UI URL, otherwise {@code null}.
     * @throws IllegalStateException Called outside the scope of an active {@link StaplerRequest}.
     */
    public static @CheckForNull
    BlueUrlTokenizer parseCurrentRequest() throws IllegalStateException {
        StaplerRequest currentRequest = Stapler.getCurrentRequest();

        if (currentRequest == null) {
            throw new IllegalStateException("Illegal call to BlueoceanUrl.parseCurrentRequest outside the scope of an active StaplerRequest.");
        }

        String path = currentRequest.getOriginalRequestURI();
        String contextPath = currentRequest.getContextPath();

        path = path.substring(contextPath.length());

        return parse(path);
    }

    /**
     * Parse the supplied URL string and return a {@link BlueUrlTokenizer} instance
     * iff the URL is a Blue Ocean UI URL.
     *
     * @param url The URL to be parsed. The URL must not be decoded in any way, so as to ensure
     *            that no URL component data is lost.
     * @return A {@link BlueUrlTokenizer} instance iff the URL is a Blue Ocean UI URL, otherwise {@code null}.
     */
    public static @CheckForNull
    BlueUrlTokenizer parse(@Nonnull String url) {
        Iterator<String> urlTokens = extractTokens(url);

        //
        // Yes, the following code is quite ugly, but it's easy enough to understand atm.
        // Unless this gets a lot more detailed, please don't get super clever ideas about using
        // some fancy-pants abstractions/patterns/3rd-party-libs for parsing the URL that, while
        // might make the code look neater structurally, also makes the code logic a lot harder
        // to follow (without using a debugger).
        //
        if (urlTokens.hasNext()) {
            if (urlTokens.next().equalsIgnoreCase("blue")) {
                BlueUrlTokenizer blueUrlTokenizer = new BlueUrlTokenizer();

                if (urlTokens.hasNext()) {
                    String next = urlTokens.next();

                    if (next.equalsIgnoreCase("pipelines")) {
                        // i.e. /blue/pipelines/
                        blueUrlTokenizer.addPart(UrlPart.DASHBOARD_PIPELINES, next);
                    } else if (next.equalsIgnoreCase("organizations")) {
                        // i.e. /blue/organizations/...
                        if (urlTokens.hasNext()) {
                            // e.g. /blue/organizations/jenkins/...
                            blueUrlTokenizer.addPart(UrlPart.ORGANIZATION, urlTokens.next());
                            if (urlTokens.hasNext()) {
                                // e.g. /blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/...
                                blueUrlTokenizer.addPart(UrlPart.PIPELINE, urlDecode(urlTokens.next()));
                                if (urlTokens.hasNext()) {
                                    next = urlTokens.next();
                                    if (next.equalsIgnoreCase("detail")) {
                                        // e.g. /blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/detail/...
                                        blueUrlTokenizer.addPart(UrlPart.PIPELINE_RUN_DETAIL, next);
                                        if (urlTokens.hasNext()) {
                                            // e.g. /blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/detail/magic-branch-X/...
                                            blueUrlTokenizer.addPart(UrlPart.BRANCH, urlDecode(urlTokens.next()));
                                            if (urlTokens.hasNext()) {
                                                // e.g. /blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/detail/magic-branch-X/55/...
                                                blueUrlTokenizer.addPart(UrlPart.PIPELINE_RUN_DETAIL_ID, urlDecode(urlTokens.next()));
                                                if (urlTokens.hasNext()) {
                                                    next = urlTokens.next();
                                                    if (PIPELINE_RUN_DETAIL_TABS.contains(next.toLowerCase())) {
                                                        // e.g. /blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/detail/magic-branch-X/55/pipeline
                                                        blueUrlTokenizer.addPart(UrlPart.PIPELINE_RUN_DETAIL_TAB, next.toLowerCase());
                                                    }
                                                }
                                            }
                                        }
                                    } else if (PIPELINE_TABS.contains(next.toLowerCase())) {
                                        // e.g. /blue/organizations/jenkins/f1%2Ff3%20with%20spaces%2Ff3%20pipeline/activity/
                                        blueUrlTokenizer.addPart(UrlPart.PIPELINE_TAB, next.toLowerCase());
                                    }
                                }
                            }
                        }
                    }
                }

                return blueUrlTokenizer;
            }
        }

        return null;
    }

    private void addPart(@Nonnull UrlPart urlPart, @Nonnull String value) {
        urlParts.put(urlPart, value);
        this.lastPart = urlPart;
    }

    public boolean hasPart(@Nonnull UrlPart urlPart) {
        return urlParts.containsKey(urlPart);
    }

    public @CheckForNull String getPart(@Nonnull UrlPart urlPart) {
        return urlParts.get(urlPart);
    }

    /**
     * Get the last {@link UrlPart} for the URL.
     * @return The last {@link UrlPart} for the URL.
     */
    public @CheckForNull UrlPart getLastPart() {
        return this.lastPart;
    }

    public boolean lastPartIs(@Nonnull UrlPart urlPart) {
        return this.lastPart == urlPart;
    }

    public boolean lastPartIs(@Nonnull UrlPart urlPart, @Nonnull String value) {
        if (this.lastPart != null && this.lastPart == urlPart) {
            String part = getPart(this.lastPart);
            return part != null && part.equals(value);
        }
        return false;
    }

    private static String urlDecode(String string) {
        try {
            return URLDecoder.decode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unexpected UnsupportedEncodingException for UTF-8.");
        }
    }

    private static Iterator<String> extractTokens(String url) {

        return Arrays.asList(url.split("/")).stream()
            .filter( s -> s.length() != 0 )
            .collect( Collectors.toList() )
            .iterator();
    }
}
