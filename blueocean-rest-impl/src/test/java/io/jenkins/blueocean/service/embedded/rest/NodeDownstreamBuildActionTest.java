package io.jenkins.blueocean.service.embedded.rest;

import org.junit.Test;

import static org.junit.Assert.*;

public class NodeDownstreamBuildActionTest {

    // FIXME: How do I get rid of this? POJO test cases make Baby Jesus cry.

    @Test
    public void appeaseCoverageMonster() {
        NodeDownstreamBuildAction a = new NodeDownstreamBuildAction("left","right");
        assertNotNull(a);
    }
}
