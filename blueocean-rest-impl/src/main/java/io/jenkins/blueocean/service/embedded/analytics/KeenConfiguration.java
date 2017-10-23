package io.jenkins.blueocean.service.embedded.analytics;

import com.google.common.collect.Iterables;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import io.keen.client.java.KeenProject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Restricted(NoExternalUse.class)
public abstract class KeenConfiguration implements ExtensionPoint {

    /** Current keen configuration */
    public static KeenConfiguration get() {
        KeenConfiguration config = Iterables.getFirst(ExtensionList.lookup(KeenConfiguration.class), null);
        if (config == null) {
            throw new IllegalStateException("no KeenConfiguration available");
        }
        return config;
    }

    /** @return projectId */
    public abstract String getProjectId();

    /** @return writeKey */
    public abstract String getWriteKey();

    /** @return project config for keen client */
    KeenProject project() {
        return new KeenProject(getProjectId(), getWriteKey(), null);
    }

    @Extension(ordinal = -1)
    public static class DefaultKeenConfiguration extends KeenConfiguration {
        @Override
        public String getProjectId() {
            return "5950934f3d5e150f5ab9d7be";
        }

        @Override
        public String getWriteKey() {
            return "E6C1FA3407AF4DD3115DBC186E40E9183A90069B1D8BBA78DB3EA6B15EA6182C881E8C55B4D7A48F55D5610AD46F36E65093227A7490BF7A56307047903BCCB16D05B9456F18A66849048F100571FDC91888CAD94F2A271A8B9E5342D2B9404E";
        }
    }
}
