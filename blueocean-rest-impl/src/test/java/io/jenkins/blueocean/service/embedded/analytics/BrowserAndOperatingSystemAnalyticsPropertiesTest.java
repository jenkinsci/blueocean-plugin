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

import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Stapler.class)
public class BrowserAndOperatingSystemAnalyticsPropertiesTest {

    @Test
    public void parsesUserAgentAndCreatesPropertiesForChrome() throws Exception {
        BrowserAndOperatingSystemAnalyticsProperties props = setup("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36");
        ImmutableMap<String, Object> actual = ImmutableMap.copyOf(props.properties(new Analytics.TrackRequest("bob", null)));
        Assert.assertEquals(6, actual.size());
        Assert.assertEquals("41", actual.get("browserVersionMajor"));
        Assert.assertEquals("10", actual.get("osVersionMinor"));
        Assert.assertEquals("Mac OS X", actual.get("osFamily"));
        Assert.assertEquals("Chrome", actual.get("browserFamily"));
        Assert.assertEquals("0", actual.get("browserVersionMinor"));
        Assert.assertEquals("10", actual.get("osVersionMajor"));
    }

    @Test
    public void parsesUserAgentAndCreatesPropertiesForSafari() throws Exception {
        BrowserAndOperatingSystemAnalyticsProperties props = setup("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A");
        ImmutableMap<String, Object> actual = ImmutableMap.copyOf(props.properties(new Analytics.TrackRequest("bob", null)));

        Assert.assertEquals(6, actual.size());
        Assert.assertEquals("7", actual.get("browserVersionMajor"));
        Assert.assertEquals("9", actual.get("osVersionMinor"));
        Assert.assertEquals("Mac OS X", actual.get("osFamily"));
        Assert.assertEquals("Safari", actual.get("browserFamily"));
        Assert.assertEquals("0", actual.get("browserVersionMinor"));
        Assert.assertEquals("10", actual.get("osVersionMajor"));
    }

    private BrowserAndOperatingSystemAnalyticsProperties setup(String userAgent) {
        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getHeader("User-Agent")).thenReturn(userAgent);
        mockStatic(Stapler.class);
        when(Stapler.getCurrentRequest()).thenReturn(request);
        return new BrowserAndOperatingSystemAnalyticsProperties();
    }
}
