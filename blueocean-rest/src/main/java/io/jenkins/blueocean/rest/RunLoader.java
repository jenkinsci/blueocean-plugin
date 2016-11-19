package io.jenkins.blueocean.rest;

import com.google.common.collect.Iterables;
import hudson.ExtensionPoint;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueRun;
import jenkins.model.Jenkins;

public abstract class RunLoader implements ExtensionPoint {

    public static RunLoader get() {
        return Iterables.getFirst(Jenkins.getInstance().getExtensionList(RunLoader.class), null);
    }

    public abstract Iterable<BlueRun> getRuns(Job job, Link parent);

    public abstract BlueRun getRun(String id, Job job, Link parent);
}
