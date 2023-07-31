package io.jenkins.blueocean.service.embedded.analytics;

import hudson.Extension;
import io.jenkins.blueocean.analytics.AdditionalAnalyticsProperties;
import io.jenkins.blueocean.analytics.Analytics.TrackRequest;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import ua_parser.Client;
import ua_parser.Parser;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

@Extension
public class BrowserAndOperatingSystemAnalyticsProperties extends AdditionalAnalyticsProperties {

    private static Parser PARSER;
    static {
        try {
            PARSER = new Parser();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Object> properties(TrackRequest req) {
        StaplerRequest httpReq = Stapler.getCurrentRequest();
        if (httpReq == null) {
            return null;
        }
        String userAgent = httpReq.getHeader("User-Agent");
        if (userAgent == null) {
            return null;
        }
        Client client = PARSER.parse(userAgent);
        String browserFamily = client.userAgent.family;
        // If we can't find the browser family then we shouldn't record anything
        if (browserFamily == null) {
            return null;
        }
        Map<String, Object> props = new HashMap<>();
        props.put("browserFamily", browserFamily);
        String browserVersionMajor = client.userAgent.major;
        // Versions are useful if they are available
        if (isNotEmpty(browserVersionMajor)) {
            props.put("browserVersionMajor", browserVersionMajor);
        }
        String browserVersionMinor = client.userAgent.minor;
        if (isNotEmpty(browserVersionMinor)) {
            props.put("browserVersionMinor", browserVersionMinor);
        }
        // If the operating system is available lets use that
        String operatingSystemFamily = client.os.family;
        if (isNotEmpty(operatingSystemFamily)) {
            props.put("osFamily", operatingSystemFamily);
            String osVersionMajor = client.os.major;
            if (isNotEmpty(osVersionMajor)) {
                props.put("osVersionMajor", osVersionMajor);
            }
            String osVersionMinor = client.os.minor;
            if (isNotEmpty(osVersionMinor)) {
                props.put("osVersionMinor", osVersionMinor);
            }
        }
        return props;
    }
}
