package io.jenkins.blueocean.config;

import hudson.Extension;
import io.jenkins.blueocean.commons.PageStatePreloader;

/**
 * {@link PageStatePreloader} for js-extensions data.
 */
@Extension
public class JenkinsJSExtensionsStatePreloader extends PageStatePreloader {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatePropertyPath() {
        return "jsExtensions";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStateJson() {
        return JenkinsJSExtensions.getExtensionsData().toString();
    }
}
