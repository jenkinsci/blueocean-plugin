package io.jenkins.blueocean.service.embedded;

import com.google.common.base.Preconditions;
import hudson.model.Action;
import hudson.model.InvisibleAction;
import hudson.model.UsageStatistics;
import io.jenkins.blueocean.analytics.Analytics;
import io.jenkins.blueocean.rest.model.BlueOceanUrlObject;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
public final class BlueOceanUrlAction implements Action {
    private final BlueOceanUrlObject blueOceanUrlObject;

    public BlueOceanUrlAction(@Nonnull BlueOceanUrlObject urlObject) {
        Preconditions.checkNotNull(urlObject);
        this.blueOceanUrlObject = urlObject;
    }

    @Override
    public String getIconFileName() {
        return blueOceanUrlObject.getIconUrl();
    }

    @Override
    public String getDisplayName() {
        return blueOceanUrlObject.getDisplayName();
    }

    @Override
    public String getUrlName() {
        return blueOceanUrlObject.getUrl();
    }

    BlueOceanUrlObject getBlueOceanUrlObject(){
        return blueOceanUrlObject;
    }

    public boolean isAnalyticsEnabled() {
        return Analytics.isAnalyticsEnabled();
    }

    private Object readResolve() {
        // Work around any actions that where erroneously persisted (JENKINS-51584)
        return DoNotShowPersistedBlueOceanUrlActions.INSTANCE;
    }

    protected static final class DoNotShowPersistedBlueOceanUrlActions extends InvisibleAction {
        private static final DoNotShowPersistedBlueOceanUrlActions INSTANCE = new DoNotShowPersistedBlueOceanUrlActions();
    }
}
