package io.jenkins.blueocean.config;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FeaturesTest {
    @Test
    public void testLookupFeature() {
        Features features = new Features();
        assertEquals("false", features.getFeature("my.cool.feature"));
    }

    @Test
    public void testSystemPropertyOverride() {
        Features features = new Features();
        System.setProperty("feature.my.cool.feature", "true");
        assertEquals("true", features.getFeature("my.cool.feature"));
    }
}
