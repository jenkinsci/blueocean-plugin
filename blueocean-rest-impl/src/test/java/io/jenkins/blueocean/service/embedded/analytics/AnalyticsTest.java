package io.jenkins.blueocean.service.embedded.analytics;

import hudson.model.UsageStatistics;
import hudson.model.User;
import io.jenkins.blueocean.analytics.Analytics;
import io.jenkins.blueocean.analytics.Analytics.TrackRequest;
import io.jenkins.blueocean.commons.MapsHelper;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.model.Jenkins;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.HashMap;
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

    // Re-enable when we want to use keen
    // @Test
    public void enableUsageStats() {
        UsageStatistics.DISABLED = false;
        Assert.assertFalse(Analytics.get() instanceof KeenAnalyticsImpl);
    }

    @Test
    public void trackWithNullAnalyticsDoesNotExplode() throws Exception {
        new NullAnalytics().track(new TrackRequest("bob", null));
    }

    @Test
    public void track() {
        Map<String, Object> props = MapsHelper.of(
            "prop1", "value1",
            "prop2", 2,
            "jenkinsVersion", j.jenkins.getVersion().toString(),
            "blueoceanVersion", Jenkins.get().getPlugin("blueocean-commons").getWrapper().getVersion()
        );
        analytics.track(new TrackRequest("test", props));

        Map<String, Object> expectedProps = new HashMap<>(props);

        Assert.assertEquals("test", analytics.lastName);
        Assert.assertEquals( expectedProps, analytics.lastProps);

        // Ensure identify does not contain the username
        Assert.assertFalse(analytics.getIdentity().contains(User.current().getId()));
    }

    @Test
    public void trackWithoutProps() {
        analytics.track(new TrackRequest("test", null));

        Map<String, Object> expectedProps = new HashMap();
        expectedProps.put("jenkinsVersion", j.jenkins.getVersion().toString());
        expectedProps.put("blueoceanVersion", Jenkins.get().getPlugin("blueocean-commons").getWrapper().getVersion());

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

        public String getIdentity() {
            return identity();
        }
    }
}
