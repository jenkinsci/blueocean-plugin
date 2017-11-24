package io.jenkins.blueocean.service.embedded.analytics;

import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import io.jenkins.blueocean.analytics.Analytics;
import io.jenkins.blueocean.analytics.Analytics.TrackRequest;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Extension
@Restricted(NoExternalUse.class)
public final class JobAnalytics extends AsyncPeriodicWork {

    static final String JOB_STATS_EVENT_NAME = "job_stats";

    public JobAnalytics() {
        super("jobAnalytics");
    }

    @Override
    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        calculateAndSend();
    }

    void calculateAndSend() {
        Analytics analytics = Analytics.get();
        if (analytics == null) {
            return;
        }
        Jenkins jenkins = Jenkins.getInstance();
        ExtensionList<JobAnalyticsCheck> checks = ExtensionList.lookup(JobAnalyticsCheck.class);
        Map<String, Integer> tally = new HashMap<>();
        jenkins.allItems().forEach(item -> checks.stream().findFirst().ifPresent(check -> {
            Integer count = tally.get(check.getName());
            if (count == null) {
                count = 1;
            } else {
                count++;
            }
            tally.put(check.getName(), count);
        }));
        analytics.track(new TrackRequest(
            JOB_STATS_EVENT_NAME,
            ImmutableMap.copyOf(tally)
        ));
    }

    @Override
    public long getRecurrencePeriod() {
        return DAY;
    }
}
