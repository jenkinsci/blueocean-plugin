package io.jenkins.blueocean.analytics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.UsageStatistics;
import hudson.model.User;
import io.jenkins.blueocean.commons.BlueOceanConfigProperties;
import io.jenkins.blueocean.commons.ServiceException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.main.modules.instance_identity.InstanceIdentity;

import javax.annotation.CheckForNull;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Facade for reporting user analytics
 */
public abstract class Analytics implements ExtensionPoint {

    private static final Logger LOGGER = Logger.getLogger(Analytics.class.getName());

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
        if (!isAnalyticsEnabled()) {
            return NullAnalytics.INSTANCE;
        }
        return ExtensionList.lookup(Analytics.class)
            .stream()
            .filter(input -> input != null && input.isEnabled()).findFirst().orElse(NullAnalytics.INSTANCE);
    }

    /** Is analytics enabled on Jenkins or not **/
    public static boolean isAnalyticsEnabled() {
        return !BlueOceanConfigProperties.isDevelopmentMode() || !UsageStatistics.DISABLED;
    }

    /** Is this analytics instance enabled */
    public abstract boolean isEnabled();

    /**
     * @param req to track
     */
    public final void track(TrackRequest req) {
        if (req == null) {
            throw new ServiceException.BadRequestException("missing request");
        }
        if (StringUtils.isEmpty(req.name)) {
            throw new ServiceException.BadRequestException("missing name");
        }
        Map<String, Object> allProps = req.properties == null ? Maps.<String, Object>newHashMap() : Maps.newHashMap(req.properties);
        // Enhance with additional properties
        for (AdditionalAnalyticsProperties enhancer : ExtensionList.lookup(AdditionalAnalyticsProperties.class)) {
            Map<String, Object> additionalProperties = enhancer.properties(req);
            if (additionalProperties != null) {
                allProps.putAll(additionalProperties);
            }
        }
        allProps.put("jenkins", server());
        allProps.put("userId", identity());
        Objects.ToStringHelper eventHelper = Objects.toStringHelper(this).add("name", req.name).add("props", allProps);
        try {
            doTrack(req.name, allProps);
            if (LOGGER.isLoggable(Level.FINE)) {
                String msg = eventHelper.toString();
                LOGGER.log(Level.FINE, msg);
            }
        } catch (Throwable throwable) {
            String msg = eventHelper.toString();
            LOGGER.log(Level.WARNING, "Failed to send event: " + msg);
        }
    }

    /**
     * @param name of the event
     * @param allProps properties to be reported
     */
    protected abstract void doTrack(String name, Map<String, Object> allProps);

    protected final String server() {
        return Hashing.sha256().hashBytes(InstanceIdentity.get().getPublic().getEncoded()).toString();
    }

    protected final String identity() {
        User user = User.current();
        String username = user == null ? "ANONYMOUS" : user.getId();
        return Hashing.sha256().hashString(username + server()).toString();
    }
}
