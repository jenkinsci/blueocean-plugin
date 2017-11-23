package io.jenkins.blueocean.service.embedded.analytics;

import hudson.ExtensionPoint;
import hudson.model.Item;

import java.util.function.Function;

public abstract class JobAnalyticsCheck implements Function<Item, Boolean>, ExtensionPoint {
    public abstract String getName();
}
