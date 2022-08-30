package io.jenkins.blueocean.service.embedded.analytics;

import io.jenkins.blueocean.analytics.Analytics;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;


public class BrowserAndOperatingSystemAnalyticsPropertiesTest {

    private MockedStatic<Stapler> staplerMockedStatic;

    @Test
    public void parsesUserAgentAndCreatesPropertiesForChrome() throws Exception {
        BrowserAndOperatingSystemAnalyticsProperties props = setup("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36");
        Map<String, Object> actual = new HashMap<>(props.properties(new Analytics.TrackRequest("bob", null)));
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
        Map<String, Object> actual = new HashMap<>(props.properties(new Analytics.TrackRequest("bob", null)));

        Assert.assertEquals(6, actual.size());
        Assert.assertEquals("7", actual.get("browserVersionMajor"));
        Assert.assertEquals("9", actual.get("osVersionMinor"));
        Assert.assertEquals("Mac OS X", actual.get("osFamily"));
        Assert.assertEquals("Safari", actual.get("browserFamily"));
        Assert.assertEquals("0", actual.get("browserVersionMinor"));
        Assert.assertEquals("10", actual.get("osVersionMajor"));
    }

    private BrowserAndOperatingSystemAnalyticsProperties setup(String userAgent) {
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        Mockito.when(request.getHeader("User-Agent")).thenReturn(userAgent);
        staplerMockedStatic = Mockito.mockStatic(Stapler.class);
        Mockito.when(Stapler.getCurrentRequest()).thenReturn(request);
        return new BrowserAndOperatingSystemAnalyticsProperties();
    }

    @After
    public void cleanup() {
        if (staplerMockedStatic!=null) {
            staplerMockedStatic.close();
        }
    }
}
