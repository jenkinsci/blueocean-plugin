package io.jenkins.blueocean.analytics;

import hudson.Extension;
import io.jenkins.blueocean.analytics.Analytics.TrackRequest;
import io.jenkins.blueocean.commons.JsonConverter;
import io.jenkins.blueocean.rest.ApiRoutable;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.io.InputStream;

@Extension
@Restricted(NoExternalUse.class)
public class AnalyticsRoute implements ApiRoutable {

    @Override
    public String getUrlName() {
        return "analytics";
    }

    @POST
    @WebMethod(name = "track")
    public void track(StaplerRequest staplerRequest) throws IOException {
        Analytics analytics = Analytics.get();
        if (analytics == null) {
            return;
        }
        TrackRequest req;
        try (InputStream is = staplerRequest.getInputStream()) {
            req = JsonConverter.toJava(is, TrackRequest.class);
        }
        analytics.track(req);
    }
}
