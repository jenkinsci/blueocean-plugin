package io.jenkins.blueocean.analytics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
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
        @JsonProperty("name")
        public final String name;
        /** properties to track with event */
        @JsonProperty("properties")
        public final Map<String, Object> properties;

        @JsonCreator
        public TrackRequest(
            @JsonProperty("name") String name,
            @JsonProperty("properties") Map<String, Object> properties
        ) {
            this.name = name;
            this.properties = properties;
        }
    }

    /**
     * @return analytics instance
     */
    @CheckForNull
    public static Analytics get() {
        return ExtensionList.lookup(Analytics.class).stream().filter(input -> input != null && input.isEnabled()).findFirst().orElseGet(() -> null);
    }

    /** Is analytics enabled on Jenkins or not **/
    public static boolean isAnalyticsEnabled() {
        return get() != null;
    }

    /** Is this analytics instance enabled */
    public abstract boolean isEnabled();

    /**
     * @param req to track
     */
    public abstract void track(TrackRequest req);
}
