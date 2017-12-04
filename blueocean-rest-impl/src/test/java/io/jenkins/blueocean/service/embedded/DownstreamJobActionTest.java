package io.jenkins.blueocean.service.embedded;

import org.junit.Test;

import static org.junit.Assert.*;

public class DownstreamJobActionTest {

    @Test
    public void constructorTest() {
        DownstreamJobAction action = new DownstreamJobAction("jobName", 123);

        assertEquals("jobName", action.getDownstreamProject());
        assertEquals(123, action.getDownstreamBuild());
    }
}
