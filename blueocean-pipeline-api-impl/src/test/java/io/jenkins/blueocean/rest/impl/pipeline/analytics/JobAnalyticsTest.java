package io.jenkins.blueocean.rest.impl.pipeline.analytics;

import io.jenkins.blueocean.analytics.Analytics;
import io.jenkins.blueocean.service.embedded.analytics.AbstractAnalytics;
import io.jenkins.blueocean.service.embedded.analytics.JobAnalytics;
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
        Assert.assertEquals("job_stats", analytics.lastReq.name);

        Map<String, Object> properties = analytics.lastReq.properties;
        Assert.assertEquals(0, properties.get("singlePipelineDeclarative"));
        Assert.assertEquals(0, properties.get("singlePipelineScripted"));
        Assert.assertEquals(0, properties.get("pipelineDeclarative"));
        Assert.assertEquals(0, properties.get("pipelineScripted"));
        Assert.assertEquals(2, properties.get("freestyle"));
        Assert.assertEquals(0, properties.get("matrix"));
        Assert.assertEquals(0, properties.get("other"));
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
