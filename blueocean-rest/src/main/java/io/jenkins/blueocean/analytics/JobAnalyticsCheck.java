package io.jenkins.blueocean.analytics;

import com.sun.org.apache.xpath.internal.operations.Bool;
import hudson.ExtensionPoint;
import hudson.model.Item;

import java.util.function.Function;

public abstract class JobAnalyticsCheck implements Function<Item, Boolean>, ExtensionPoint {
    public abstract String getName();
}
