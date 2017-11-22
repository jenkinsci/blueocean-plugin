package io.jenkins.blueocean.service.embedded.analytics;

import com.google.common.collect.ImmutableMap;
import hudson.ExtensionList;
import io.jenkins.blueocean.analytics.Analytics;
import io.jenkins.blueocean.analytics.Analytics.TrackRequest;
import io.jenkins.blueocean.analytics.JobAnalyticsCheck;
import jenkins.model.Jenkins;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class JobAnalytics implements Runnable {

    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public JobAnalytics() {
        this.service.scheduleAtFixedRate(this, 0, 1, TimeUnit.DAYS);
    }

    @Override
    public void run() {
        Analytics analytics = Analytics.get();
        if (analytics == null) {
            return;
        }
        Jenkins jenkins = Jenkins.getInstance();
        ExtensionList<JobAnalyticsCheck> checks = ExtensionList.lookup(JobAnalyticsCheck.class);
        Map<String, Integer> tally = new HashMap<>();
        jenkins.getAllItems().forEach(item -> checks.stream().filter(check -> check.apply(item)).findFirst().ifPresent(check -> {
            Integer count = tally.get(check.getName());
            if (count == null) {
                count = 1;
            } else {
                count++;
            }
            tally.put(check.getName(), count);
        }));
        analytics.track(new TrackRequest(
            "jobStats",
            ImmutableMap.copyOf(tally)
        ));
    }
}
