package io.jenkins.blueocean.service.embedded.analytics;

import io.jenkins.blueocean.commons.JsonConverter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class AnalyticsConfigTest {
    @Test
    public void serializeDeseralize() {
        AnalyticsConfig config = new AnalyticsConfig(1000, Arrays.asList("10", "20", "30-50"));
        String json = JsonConverter.toJson(config);
        AnalyticsConfig deserializedConfig = JsonConverter.toJava(json, AnalyticsConfig.class);

        Assert.assertEquals(config.cohorts, deserializedConfig.cohorts);

        deserializedConfig.allActiveCohorts();

        Assert.assertEquals(3, config.activeCohorts.size());

        Assert.assertEquals("10", config.activeCohorts.get(0));
        Assert.assertEquals("20", config.activeCohorts.get(1));
        Assert.assertEquals("30-50", config.activeCohorts.get(2));
    }
}
