package io.jenkins.blueocean.service.embedded.analytics;

import io.jenkins.blueocean.analytics.Analytics;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;

import java.util.Map;

public class JobAnalyticsTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testJobAnalytics() throws Exception {
        j.createFreeStyleProject("freestyle1");
        j.createFreeStyleProject("freestyle2");

        AnalyticsImpl analytics = (AnalyticsImpl)Analytics.get();
        Assert.assertNotNull(analytics);

        JobAnalytics jobAnalytics = new JobAnalytics();
        jobAnalytics.calculateAndSend();

        Assert.assertNotNull(analytics.lastReq);
        Assert.assertEquals(JobAnalytics.JOB_STATS_EVENT_NAME, analytics.lastReq.name);

        Map<String, Object> properties = analytics.lastReq.properties;
        Assert.assertEquals(2, properties.get("freestyle"));
    }

    @TestExtension
    public static class AnalyticsImpl extends AbstractAnalytics {

        TrackRequest lastReq;

        @Override
        protected void doTrack(String name, Map<String, Object> allProps) {
            lastReq = new TrackRequest(name, allProps);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
