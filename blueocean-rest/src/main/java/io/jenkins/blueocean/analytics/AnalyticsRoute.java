package io.jenkins.blueocean.analytics;

import hudson.Extension;
import io.jenkins.blueocean.analytics.Analytics.TrackRequest;
import io.jenkins.blueocean.commons.JsonConverter;
import io.jenkins.blueocean.rest.ApiRoutable;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.io.InputStreamReader;

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
        TrackRequest req;
        try (InputStreamReader reader = new InputStreamReader(staplerRequest.getInputStream())) {
            req = JsonConverter.toJava(reader, TrackRequest.class);
        }
        Analytics.get().track(req);
    }
}
