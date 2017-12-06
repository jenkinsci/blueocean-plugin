package io.jenkins.blueocean.service.embedded.analytics;

import hudson.ExtensionPoint;
import hudson.model.Item;

import java.util.function.Function;

/** Used to exclude an item from {@link io.jenkins.blueocean.service.embedded.analytics.JobAnalytics} **/
public interface JobAnalyticsExclude extends Function<Item, Boolean>, ExtensionPoint {
}
