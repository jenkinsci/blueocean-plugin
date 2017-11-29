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
import java.util.HashMap;
import java.util.Map;

@Extension
@Restricted(NoExternalUse.class)
public final class JobAnalytics extends AsyncPeriodicWork {

    private static final String JOB_STATS_EVENT_NAME = "job_stats";

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

        // Initialize the tally
        Map<String, Integer> tally = new HashMap<>();
        checks.forEach(check -> tally.put(check.getName(), 0));
        tally.put("other", 0);

        jenkins.allItems().forEach(item -> {
            if (item instanceof AbstractProject // must be a project
                && !item.getClass().getName().equals("hudson.matrix.MatrixConfiguration")  // Individual matrix configurations
                && !item.getClass().getName().equals("hudson.maven.MavenModule")) // Ignore maven modules)
            {
                boolean matchFound = false;
                for (JobAnalyticsCheck check : checks) {
                    if (check.apply(item)) {
                        addToTally(tally, check.getName());
                        matchFound = true;
                        break;
                    }
                }
                if (!matchFound) {
                    addToTally(tally, "other");
                }
            }
        });
        analytics.track(new TrackRequest(
            JOB_STATS_EVENT_NAME,
            ImmutableMap.copyOf(tally)
        ));
    }

    private void addToTally(Map<String, Integer> tally, String name) {
        Integer count = tally.get(name);
        if (count == null) {
            count = 0;
        }
        tally.put(name, count + 1);
    }

    @Override
    public long getRecurrencePeriod() {
        return DAY;
    }
}
