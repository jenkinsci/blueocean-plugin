package io.jenkins.blueocean.service.embedded.analytics;

import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import io.keen.client.java.KeenProject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

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
    public final static class DefaultKeenConfiguration extends KeenConfiguration {

        private final String projectId;
        private final String writeKey;

        public DefaultKeenConfiguration() throws IOException {
            URL url = Resources.getResource(KeenAnalyticsImpl.class, "jenkins-analytics.properties");
            Properties properties = new Properties();
            try (InputStream is = Resources.newInputStreamSupplier(url).getInput()) {
                properties.load(is);
            }
            this.projectId = properties.getProperty("projectId");
            this.writeKey = properties.getProperty("writeKey");
        }

        @Override
        public String getProjectId() {
            return projectId;
        }

        @Override
        public String getWriteKey() {
            return writeKey;
        }
    }
}
