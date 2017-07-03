package io.jenkins.blueocean.analytics;

import hudson.Extension;
import io.jenkins.blueocean.analytics.Analytics.TrackRequest;
import io.jenkins.blueocean.rest.ApiRoutable;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.POST;

@Extension
@Restricted(NoExternalUse.class)
public class AnalyticsRoute implements ApiRoutable {

    @Override
    public String getUrlName() {
        return "analytics";
    }

    @POST
    @WebMethod(name = "track")
    public void track(TrackRequest req) {
        Analytics.get().track(req);
    }
}
