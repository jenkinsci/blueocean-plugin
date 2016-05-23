package io.jenkins.blueocean.rest.model.queue;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.ApiRoutable;
import io.jenkins.blueocean.rest.model.Container;

public abstract class BlueTaskContainer extends Container<BlueTask> implements ApiRoutable, ExtensionPoint {
    @Override
    public String getUrlName() {
        return "tasks";
    }
}
