package io.jenkins.blueocean.analytics;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Facade for reporting user analytics
 */
public abstract class Analytics implements ExtensionPoint {

    /**
     * Track a user event and properties
     */
    public static class TrackRequest {
        /** event name **/
        public final String name;
        /** properties to track with event */
        public final Map<String, Object> properties;

        @DataBoundConstructor
        public TrackRequest(String name, Map<String, Object> properties) {
            this.name = name;
            this.properties = properties;
        }
    }

    /**
     * @return analytics instance
     */
    public static Analytics get() {
        return Iterables.find(ExtensionList.lookup(Analytics.class), new Predicate<Analytics>() {
            @Override
            public boolean apply(@Nullable Analytics input) {
                return input != null && input.isEnabled();
            }
        });
    }

    /** Is this analytics instance enabled */
    public abstract boolean isEnabled();

    /**
     * @param req to track
     */
    public abstract void track(TrackRequest req);
}
