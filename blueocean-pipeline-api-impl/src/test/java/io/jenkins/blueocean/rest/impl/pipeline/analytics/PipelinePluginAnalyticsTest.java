package io.jenkins.blueocean.rest.impl.pipeline.analytics;

import hudson.model.CauseAction;
import io.jenkins.blueocean.analytics.Analytics;
import io.jenkins.blueocean.analytics.Analytics.TrackRequest;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.service.embedded.analytics.AbstractAnalytics;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PipelinePluginAnalyticsTest extends PipelineBaseTest {

    @Test(timeout = 120000)
    public void testGenerateAnalyticsEventWithDeclarative() throws Exception {
        // Create single scripted pipeline
        createAndRunPipeline("JobAnalyticsTest-declarative.jenkinsfile");

        AnalyticsImpl analytics = (AnalyticsImpl) Analytics.get();
        Assert.assertNotNull(analytics);

        TrackRequest req = analytics.lastReq;
        Assert.assertNotNull(req);
        Assert.assertEquals("pipeline_step_used", req.name);

        Map<String, Object> properties = req.properties;
        Assert.assertEquals("type", "org.jenkinsci.plugins.workflow.steps.EchoStep", properties.get("type"));
        Assert.assertEquals("timesUsed", 2, properties.get("timesUsed"));
        Assert.assertEquals("isDeclarative", true, properties.get("isDeclarative"));
        Assert.assertEquals("runResult", "SUCCESS", properties.get("runResult"));
    }

    @Test(timeout = 120000)
    public void testGenerateAnalyticsEvent() throws Exception {
        // Create single scripted pipeline
        createAndRunPipeline("JobAnalyticsTest-scripted.jenkinsfile");

        AnalyticsImpl analytics = (AnalyticsImpl) Analytics.get();
        Assert.assertNotNull(analytics);

        TrackRequest req = analytics.lastReq;
        Assert.assertNotNull(req);
        Assert.assertEquals("pipeline_step_used", req.name);

        Map<String, Object> properties = req.properties;
        Assert.assertEquals("type", "org.jenkinsci.plugins.workflow.steps.EchoStep", properties.get("type"));
        Assert.assertEquals("timesUsed", 2, properties.get("timesUsed"));
        Assert.assertEquals("isDeclarative", false, properties.get("isDeclarative"));
        Assert.assertEquals("runResult", "SUCCESS", properties.get("runResult"));
    }

    @Test(timeout = 120000)
    public void testGeneratedAnalyticsEventWithScriptedFunction() throws Exception {
        createAndRunPipeline("PipelinePluginAnalyticsTest-scripted-function.jenkinsfile");

        AnalyticsImpl analytics = (AnalyticsImpl) Analytics.get();
        Assert.assertNotNull(analytics);

        TrackRequest req = analytics.lastReq;
        Assert.assertNotNull(req);
        Assert.assertEquals("pipeline_step_used", req.name);

        Map<String, Object> properties = req.properties;
        Assert.assertEquals("type", "org.jenkinsci.plugins.workflow.steps.EchoStep", properties.get("type"));
        Assert.assertEquals("timesUsed", 1, properties.get("timesUsed"));
        Assert.assertEquals("isDeclarative", false, properties.get("isDeclarative"));
        Assert.assertEquals("runResult", "SUCCESS", properties.get("runResult"));
    }

    private void createAndRunPipeline(String jenkinsFileName) throws java.io.IOException, InterruptedException, java.util.concurrent.ExecutionException {
        // Create the pipeline and run it
        WorkflowJob scriptedSingle = createWorkflowJobWithJenkinsfile(getClass(), jenkinsFileName);
        WorkflowRun scriptedSingleRun = scriptedSingle.scheduleBuild2(0, new CauseAction()).waitForStart();
        j.waitForCompletion(scriptedSingleRun);
        // RunListeners can sometimes be executed after the run is reported as completed. This fixes a race condition.
        AnalyticsImpl analytics = (AnalyticsImpl) Analytics.get();
        Assert.assertNotNull(analytics);
        while (analytics.lastReq == null) {
            Thread.sleep(100);
        }
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
