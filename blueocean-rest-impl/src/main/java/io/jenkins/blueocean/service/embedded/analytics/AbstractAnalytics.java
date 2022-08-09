package io.jenkins.blueocean.service.embedded.analytics;

import hudson.ExtensionList;
import hudson.model.UsageStatistics;
import hudson.model.User;
import io.jenkins.blueocean.analytics.AdditionalAnalyticsProperties;
import io.jenkins.blueocean.analytics.Analytics;
import io.jenkins.blueocean.commons.DigestUtils;
import io.jenkins.blueocean.commons.ServiceException;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.Stapler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


/**
 * Implements {@link Analytics} to guarantee common properties are tracked with any events sent
 */
@Restricted(NoExternalUse.class)
public abstract class AbstractAnalytics extends Analytics {

    private static final Logger LOGGER = LoggerFactory.getLogger( AbstractAnalytics.class.getName());

    public boolean isEnabled() {
        return !UsageStatistics.DISABLED;
    }

    /**
     * @param req to track
     */
    public void track(TrackRequest req) {
        if (req == null) {
            throw new ServiceException.BadRequestException("missing request");
        }
        if (StringUtils.isEmpty(req.name)) {
            throw new ServiceException.BadRequestException("missing name");
        }
        Map<String, Object> allProps = req.properties == null ? new HashMap<>() : new HashMap<>(req.properties);
        // Enhance with additional properties
        for (AdditionalAnalyticsProperties enhancer : ExtensionList.lookup(AdditionalAnalyticsProperties.class)) {
            Map<String, Object> additionalProperties = enhancer.properties(req);
            if (additionalProperties != null) {
                allProps.putAll(additionalProperties);
            }
        }
        // Background requests do not have userId
        if (Stapler.getCurrentRequest() != null) {
            String identity = identity();
            allProps.put("userId", identity);
        }

        try {
            doTrack(req.name, allProps);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}, name: {}, props: {}", getClass().getSimpleName(), req.name, allProps);
            }
        } catch (Throwable throwable) {
            LOGGER.warn("Failed to send event: {}, name: {}, props: {}", getClass().getSimpleName(), req.name, allProps);
            LOGGER.warn("Failed to send event", throwable);
        }
    }

    protected abstract void doTrack(String name, Map<String, Object> allProps);

    protected final String identity() {
        User user = User.current();
        String username = user == null ? "ANONYMOUS" : user.getId();
        return DigestUtils.sha256(username, Charset.defaultCharset());
    }
}
