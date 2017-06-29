package io.jenkins.blueocean.service.embedded.analytics;

import hudson.ExtensionList;
import io.jenkins.blueocean.service.embedded.analytics.KeenConfiguration.DefaultKeenConfiguration;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;

public class KeenAnalyticsTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void keenConfigCanBeOverridden() {
        // Check that the instance returned is the test extension
        KeenConfiguration config = KeenConfiguration.get();
        Assert.assertEquals("coolProject", config.getProjectId());
        Assert.assertEquals("myWriteKey", config.getWriteKey());
        // Ensure that only two are found
        ExtensionList<KeenConfiguration> configs = ExtensionList.lookup(KeenConfiguration.class);
        Assert.assertEquals(2, configs.size());
        // Ensure ordering is correct
        Assert.assertTrue(configs.get(0) instanceof TestKeenConfiguration);
        Assert.assertTrue(configs.get(1) instanceof DefaultKeenConfiguration);
    }

    @TestExtension
    public static class TestKeenConfiguration extends KeenConfiguration {
        @Override
        public String getProjectId() {
            return "coolProject";
        }

        @Override
        public String getWriteKey() {
            return "myWriteKey";
        }
    }
}
