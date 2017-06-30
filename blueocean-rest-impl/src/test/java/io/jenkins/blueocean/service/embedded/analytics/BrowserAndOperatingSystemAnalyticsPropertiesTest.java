package io.jenkins.blueocean.service.embedded.analytics;

import com.google.common.collect.ImmutableMap;
import io.jenkins.blueocean.analytics.Analytics;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Stapler.class)
public class BrowserAndOperatingSystemAnalyticsPropertiesTest {

    @Test
    public void parsesUserAgentAndCreatesPropertiesForChrome() throws Exception {
        Map<String, Object> expected = ImmutableMap.<String, Object>builder()
            .put("browserVersionMajor", 41)
            .put("osVersionMinor", 10)
            .put("osFamily", "Mac OS X")
            .put("browserFamily", "Chrome")
            .put("browserVersionMinor", 0)
            .put("osVersionMajor", 10)
            .build();
        BrowserAndOperatingSystemAnalyticsProperties props = setup("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36");
        ImmutableMap<String, Object> actual = ImmutableMap.copyOf(props.properties(new Analytics.TrackRequest("bob", null)));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void parsesUserAgentAndCreatesPropertiesForSafari() throws Exception {
        Map<String, Object> expected = ImmutableMap.<String, Object>builder()
            .put("browserVersionMajor", 7)
            .put("osVersionMinor", 9)
            .put("osFamily", "Mac OS X")
            .put("browserFamily", "Safari")
            .put("browserVersionMinor", 0)
            .put("osVersionMajor", 10)
            .build();
        BrowserAndOperatingSystemAnalyticsProperties props = setup("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A");
        ImmutableMap<String, Object> actual = ImmutableMap.copyOf(props.properties(new Analytics.TrackRequest("bob", null)));
        Assert.assertEquals(expected.entrySet(), actual.entrySet());
    }

    private BrowserAndOperatingSystemAnalyticsProperties setup(String userAgent) {
        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getHeader("User-Agent")).thenReturn(userAgent);
        mockStatic(Stapler.class);
        when(Stapler.getCurrentRequest()).thenReturn(request);
        return new BrowserAndOperatingSystemAnalyticsProperties();
    }
}
