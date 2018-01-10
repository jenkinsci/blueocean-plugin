package io.jenkins.blueocean.service.embedded.analytics;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class TallyTest {
    @Test
    public void tally() {
        Tally tally = new Tally();

        tally.count("foo");
        tally.count("foo");
        tally.count("bar");
        tally.count("bar");
        tally.count("bar");
        tally.count("bar");
        tally.zero("baz");

        Map<String, Object> total = tally.get();
        Assert.assertEquals(3, total.size());
        Assert.assertEquals(2, total.get("foo"));
        Assert.assertEquals(4, total.get("bar"));
        Assert.assertEquals(0, total.get("baz"));
    }
}
