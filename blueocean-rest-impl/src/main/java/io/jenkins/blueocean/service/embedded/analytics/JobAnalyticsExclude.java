package io.jenkins.blueocean.service.embedded.analytics;

import hudson.ExtensionPoint;
import hudson.model.Item;

import java.util.function.Function;

/** Used to exclude an item from {@link io.jenkins.blueocean.service.embedded.analytics.JobAnalytics} **/
public abstract class JobAnalyticsExclude implements Function<Item, Boolean>, ExtensionPoint {
}
