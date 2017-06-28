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
            return "595421390ee24f5b59032f79";
        }

        @Override
        public String getWriteKey() {
            return "3B9A2F5ECEEF0E4C22886A644443F1C994FE3D294C4A0520699671A452C4A017E0C28196ADF8DEBC31547BFC0BC32BE824E6C7300C3E3FB2C5D921B74F274FA95B63C17E0349AA970CDB35FDE519A364A26958B488E9A729BFD03A034BE42F56";
        }
    }
}
