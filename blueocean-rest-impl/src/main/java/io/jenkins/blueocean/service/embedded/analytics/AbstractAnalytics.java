package io.jenkins.blueocean.service.embedded.analytics;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import hudson.model.User;
import hudson.util.VersionNumber;
import io.jenkins.blueocean.analytics.Analytics;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.main.modules.instance_identity.InstanceIdentity;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements {@link Analytics} to guarantee common properties are tracked with any events sent
 */
@Restricted(NoExternalUse.class)
public abstract class AbstractAnalytics extends Analytics {

    private static final Logger LOGGER = Logger.getLogger(AbstractAnalytics.class.getName());

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
        VersionNumber version = Jenkins.getVersion();
        if (version != null && version.toString() != null) {
            allProps.put("jenkinsVersion", version.toString());
        }
        allProps.put("blueoceanVersion", Jenkins.getInstance().getPlugin("blueocean-rest-impl").getWrapper().getVersion());
        String msg = Objects.toStringHelper(this).add("name", req.name).add("props", allProps).toString();
        try {
            doTrack(req.name, allProps);
            LOGGER.log(Level.FINE, msg);
        } catch (Throwable throwable) {
            LOGGER.log(Level.WARNING, "Failed to send event: " + msg);
        }
    }

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
