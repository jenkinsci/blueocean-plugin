package io.jenkins.blueocean;

import hudson.ExtensionList;
import hudson.Main;
import io.jenkins.blueocean.dev.RunBundleWatches;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

/**
 * Root of Blue Ocean UI
 *
 * @author Kohsuke Kawaguchi
 */
public class BlueOceanUI {
    private static final Logger logger = LoggerFactory.getLogger(BlueOceanUI.class);

    private volatile BlueOceanUIProvider provider;

    public BlueOceanUI() {
        ResourceCacheControl.install();
    }

    /**
     * Exposes {@link RootRoutable}s to the URL space. Returns <code>this</code> if none found, allowing the UI to
     * resolve routes. This also has the side effect that we won't be able to generate 404s for any URL that *might*
     * resolve to a valid UI route. If and when we implement server-side rendering of initial state or to solidify the
     * routes on the back-end for real 404s, we'll need to complicate this behaviour :D
     */
    public Object getDynamic(String route) {
        // JVM will optimize this branch out typically:
        if (RunBundleWatches.isEnabled) {
            RunBundleWatches.waitForScriptBuilds();
        }
        for (RootRoutable r : ExtensionList.lookup(RootRoutable.class)) {
            if (r.getUrlName().equals(route))
                return r;
        }
        return this;
    }

    /**
     * The base of all BlueOcean URLs (underneath wherever Jenkins itself is deployed).
     */
    public String getUrlBase() {
        setBlueOceanUIProvider();
        if(provider == null){
            logger.error("BlueOceanUIProvider extension not found");
            return null;
        }
        return provider.getUrlBasePrefix();
    }

    /**
     * Have some slightly different behavior in development mode
     */
    public boolean isDevelopmentMode() {
        return Main.isDevelopmentMode || System.getProperty("hudson.hpi.run") != null; // TODO why isDevelopmentMode == false
    }

    /**
     * Get the language associated with the current page.
     * @return The language string.
     */
    public String getLang() {
        StaplerRequest currentRequest = Stapler.getCurrentRequest();

        if (currentRequest != null) {
            Locale locale = currentRequest.getLocale();
            if (locale != null) {
                return locale.toLanguageTag();
            }
        }

        return null;
    }

    public List<BluePageDecorator> getPageDecorators(){
        return BluePageDecorator.all();
    }

    public long getNow() {
         return System.currentTimeMillis();
    }

    private void setBlueOceanUIProvider(){
        BlueOceanUIProvider boui = provider;
        if(boui == null){
            synchronized (this){
                boui = provider;
                if(boui == null){
                    for(BlueOceanUIProvider p: BlueOceanUIProvider.all()){
                        provider = boui = p;
                        return;
                    }
                }
            }
        }
    }
}
