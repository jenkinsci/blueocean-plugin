package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterators;
import com.google.common.io.Resources;
import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueRunFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.net.URL;

public class BlueJUnitTestResultTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void createsTestResult() throws Exception {
        URL resource = Resources.getResource(getClass(), "BlueJUnitTestResultTest.jenkinsfile");
        String jenkinsFile = Resources.toString(resource, Charsets.UTF_8);

        WorkflowJob p = j.createProject(WorkflowJob.class, "project");
        p.setDefinition(new CpsFlowDefinition(jenkinsFile, false));
        p.save();

        Run r = p.scheduleBuild2(0).waitForStart();
        j.waitUntilNoActivity();

        BlueRun test = BlueRunFactory.getRun(r, new Reachable() {
            @Override
            public Link getLink() {
                return new Link("test");
            }
        });

        Assert.assertEquals(3, Iterators.size(test.getTests().iterator()));
    }
}
