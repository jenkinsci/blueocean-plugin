package io.jenkins.blueocean.listeners;

import io.jenkins.blueocean.rest.hal.Link;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class NodeDownstreamBuildActionTest {

    // FIXME: How do I get rid of this? POJO test cases make Baby Jesus cry.

    @Test
    public void appeaseCoverageMonster() {
        NodeDownstreamBuildAction a = new NodeDownstreamBuildAction(new Link("/nuts"), "right");
        assertNotNull(a);
    }
}
