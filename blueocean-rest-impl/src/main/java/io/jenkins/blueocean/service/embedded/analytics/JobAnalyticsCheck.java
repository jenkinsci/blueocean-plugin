package io.jenkins.blueocean.service.embedded.analytics;

import hudson.ExtensionPoint;
import hudson.model.Item;

import java.util.function.Function;

/** Used to check the type of item for {@link io.jenkins.blueocean.service.embedded.analytics.JobAnalytics} **/
public interface JobAnalyticsCheck extends Function<Item, Boolean>, ExtensionPoint {
    String getName();
}
