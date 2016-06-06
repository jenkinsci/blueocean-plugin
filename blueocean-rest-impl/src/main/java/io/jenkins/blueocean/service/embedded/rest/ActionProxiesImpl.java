package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Action;
import io.jenkins.blueocean.rest.model.BlueActionProxy;

/**
 * @author Vivek Pandey
 */
public class ActionProxiesImpl extends BlueActionProxy {

    private final Action action;
    public ActionProxiesImpl(Action action) {
        this.action = action;
    }


    @Override
    public Object getAction() {
        return action;
    }

    @Override
    public String getUrlName() {
        return action.getUrlName();
    }

    @Override
    public String get_Class() {
        return action.getClass().getName();
    }
}
