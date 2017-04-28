package io.jenkins.blueocean;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * BlueOcean UI provider.
 *
 * Provides basic UI configuration.
 *
 * @author Vivek Pandey
 */
public abstract class BlueOceanUIProvider implements ExtensionPoint {

    /**
     * Root url where Jenkins is hosted. Must end with '/' suffix.
     *
     * @return it can return null if request is not made in context of HTTP request or root url is not configured.
     */
    public @CheckForNull abstract String getRootUrl();

    /**
     * Gives url base prefix where blueocean is hosted. e.g. "blue".
     *
     * It must not include '/' prefix or suffix.
     *
     * @return url base
     */
    public @Nonnull abstract String getUrlBasePrefix();

    /**
     * Gives landing page path. This path is suffixes to  getUrlBase().
     *
     * e.g. if the landing page is at URL, http://localhost:8080/jenkins/blue/organization/org1/pipelines/ then this
     * method should return "/organization/org1/pipelines/".
     *
     * Must have '/' prefix and suffix.
     *
     * @return landing page URL path
     */
    public @Nonnull abstract String getLandingPagePath();

    public static ExtensionList<BlueOceanUIProvider> all(){
        return ExtensionList.lookup(BlueOceanUIProvider.class);
    }
}
