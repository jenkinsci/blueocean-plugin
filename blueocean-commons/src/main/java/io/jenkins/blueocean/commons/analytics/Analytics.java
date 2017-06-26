package io.jenkins.blueocean.commons.analytics;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import hudson.model.UsageStatistics;
import hudson.model.User;
import io.jenkins.blueocean.commons.ServiceException;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.main.modules.instance_identity.InstanceIdentity;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Restricted(NoExternalUse.class)
public abstract class Analytics {

    private static final Logger LOGGER = Logger.getLogger(Analytics.class.getName());

    public static class TrackRequest {
        public final String name;
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
        return UsageStatistics.DISABLED ? NullAnalytics.INSTANCE : KeenAnalyticsImpl.INSTANCE;
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
        Map<String, Object> allProps = req.properties == null ? Maps.<String, Object>newHashMap() : Maps.newHashMap(req.properties);
        allProps.put("jenkins", server());
        allProps.put("userId", identity());
        doTrack(req.name, allProps);
        LOGGER.log(Level.FINE, Objects.toStringHelper(this).add("name", req.name).add("props", allProps).toString());
    }

    protected abstract void doTrack(String name, Map<String, Object> allProps);

    protected final String server() {
        return InstanceIdentity.get().getPublic().getPublicExponent().toString(16);
    }

    protected final String identity() {
        User user = User.current();
        String username = user == null ? "ANONYMOUS" : user.getId();
        return Hashing.sha256().hashString(username + server()).toString();
    }
}
