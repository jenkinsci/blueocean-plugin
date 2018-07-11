package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueRunFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultRunImplTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void unknownRunTypeResolvesToDefaultRunImpl() throws Exception {
        Run run = mock(Run.class);
        when(run.getParent()).thenReturn(j.createFreeStyleProject());

        BlueRun blueRun = BlueRunFactory.getRun(run, () -> new Link("foo"));
        Assert.assertNotNull(blueRun);
        Assert.assertTrue(blueRun instanceof DefaultRunImpl);
    }
}
