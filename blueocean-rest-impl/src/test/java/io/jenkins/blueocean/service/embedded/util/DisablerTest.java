package io.jenkins.blueocean.service.embedded.util;

import hudson.model.Job;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.springframework.util.Assert;

import java.io.IOException;

public class DisablerTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void Freestyle() throws IOException {
        Job freestyle = j.createFreeStyleProject("Freestyle");
        Assert.isTrue(!Disabler.isDisabled(freestyle), "Newly created job should not be disabled by default");
        Disabler.makeDisabled(freestyle, true);
        Assert.isTrue(Disabler.isDisabled(freestyle), "After disable it should be disabled");
    }

    @Test
    public void Pipeline() throws IOException {
        Job job = j.createProject(WorkflowJob.class, "Pipeline");
        Assert.isTrue(!Disabler.isDisabled(job), "Newly created job should not be disabled by default");
        Disabler.makeDisabled(job, true);
        Assert.isTrue(Disabler.isDisabled(job), "After disable it should be disabled");
    }
}
