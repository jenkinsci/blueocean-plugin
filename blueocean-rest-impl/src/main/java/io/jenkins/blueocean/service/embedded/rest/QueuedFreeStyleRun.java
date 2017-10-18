package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.hal.Link;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_FREE_STYLE_BUILD;

@Capability(JENKINS_FREE_STYLE_BUILD)
public class QueuedFreeStyleRun extends QueuedBlueRun {
    public QueuedFreeStyleRun(BlueRunState runState, BlueRunResult runResult, QueueItemImpl item, Link parent) {
        super(runState, runResult, item, parent);
    }
}
