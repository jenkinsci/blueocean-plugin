package io.jenkins.blueocean.service.embedded.analytics;

import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.AbstractProject;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import io.jenkins.blueocean.analytics.Analytics;
import io.jenkins.blueocean.analytics.Analytics.TrackRequest;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.io.IOException;
import java.util.Map;

@Extension
@Restricted(NoExternalUse.class)
public final class JobAnalytics extends AsyncPeriodicWork {

    private static final String JOB_STATS_EVENT_NAME = "job_stats";
    private static final String OTHER_CATEGORY = "other";

    public JobAnalytics() {
        super("jobAnalytics");
    }

    @Override
    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        calculateAndSend();
    }

    public void calculateAndSend() {
        Analytics analytics = Analytics.get();
        if (analytics == null) {
            return;
        }
        Jenkins jenkins = Jenkins.getInstance();
        ExtensionList<JobAnalyticsCheck> checks = ExtensionList.lookup(JobAnalyticsCheck.class);
        ExtensionList<JobAnalyticsExclude> excludes = ExtensionList.lookup(JobAnalyticsExclude.class);

        // Initialize the tally
        Tally tally = new Tally();
        checks.forEach(check -> tally.zero(check.getName()));
        tally.zero(OTHER_CATEGORY);

        jenkins.allItems().forEach(item -> {
            if (excludes.stream().noneMatch(exclude -> exclude.apply(item))) {
                boolean matchFound = false;
                for (JobAnalyticsCheck check : checks) {
                    if (check.apply(item)) {
                        tally.count(check.getName());
                        matchFound = true;
                        break;
                    }
                }
                if (!matchFound) {
                    tally.count(OTHER_CATEGORY);
                }
            }
        });
        analytics.track(new TrackRequest(
            JOB_STATS_EVENT_NAME,
            ImmutableMap.copyOf(tally.get())
        ));
    }

    @Override
    public long getRecurrencePeriod() {
        return DAY;
    }
}
