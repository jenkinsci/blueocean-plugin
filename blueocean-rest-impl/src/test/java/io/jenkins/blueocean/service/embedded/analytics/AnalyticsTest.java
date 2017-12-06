package io.jenkins.blueocean.service.embedded.analytics;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import hudson.model.UsageStatistics;
import hudson.model.User;
import io.jenkins.blueocean.analytics.Analytics;
import io.jenkins.blueocean.analytics.Analytics.TrackRequest;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.model.Jenkins;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Map;

public class AnalyticsTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    MyAnalytics analytics;

    @Before
    public void setup() {
        analytics = new MyAnalytics();
    }

    @Test
    public void disableUsageStats() {
        UsageStatistics.DISABLED = true;
        Assert.assertTrue(Analytics.get() instanceof NullAnalytics);
    }

    @Test
    public void enableUsageStats() {
        UsageStatistics.DISABLED = false;
        Assert.assertTrue(Analytics.get() instanceof KeenAnalyticsImpl);
    }

    @Test
    public void trackWithNullAnalyticsDoesNotExplode() throws Exception {
        new NullAnalytics().track(new TrackRequest("bob", null));
    }

    @Test
    public void track() {
        ImmutableMap<String, Object> props = ImmutableMap.<String, Object>of(
            "prop1", "value1",
            "prop2", 2,
            "jenkinsVersion", j.jenkins.getVersion().toString(),
            "blueoceanVersion", Jenkins.getInstance().getPlugin("blueocean-commons").getWrapper().getVersion()
        );
        analytics.track(new TrackRequest("test", props));

        Map<String, Object> expectedProps = Maps.newHashMap(props);
        expectedProps.put("jenkins", analytics.getServer());

        Assert.assertEquals("test", analytics.lastName);
        Assert.assertEquals( expectedProps, analytics.lastProps);

        // Ensure identify does not contain the username
        Assert.assertFalse(analytics.getIdentity().contains(User.current().getId()));
    }

    @Test
    public void trackWithoutProps() {
        analytics.track(new TrackRequest("test", null));

        Map<String, Object> expectedProps = Maps.newHashMap();
        expectedProps.put("jenkins", analytics.getServer());
        expectedProps.put("jenkinsVersion", j.jenkins.getVersion().toString());
        expectedProps.put("blueoceanVersion", Jenkins.getInstance().getPlugin("blueocean-commons").getWrapper().getVersion());

        Assert.assertEquals("test", analytics.lastName);
        Assert.assertEquals( expectedProps, analytics.lastProps);

        // Ensure identify does not contain the username
        Assert.assertFalse(analytics.getIdentity().contains(User.current().getId()));
    }

    @Test
    public void nullTrackRequest() {
        try {
            analytics.track(null);
            Assert.fail("did not throw exception");
        } catch (ServiceException.BadRequestException e) {
            Assert.assertEquals("missing request", e.getMessage());
        }
    }

    @Test
    public void nullNameInTrackRequest() {
        try {
            analytics.track(new TrackRequest(null, null));
            Assert.fail("did not throw exception");
        } catch (ServiceException.BadRequestException e) {
            Assert.assertEquals("missing name", e.getMessage());
        }
    }

    class MyAnalytics extends AbstractAnalytics {

        String lastName;
        Map<String, Object> lastProps;

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        protected void doTrack(String name, Map<String, Object> allProps) {
            lastName = name;
            lastProps = allProps;
        }

        public String getServer() {
            return server();
        }

        public String getIdentity() {
            return identity(server());
        }
    }
}
