package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Action;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * @author Vivek Pandey
 */
public class ActionProxiesImpl extends BlueActionProxy {

    private final Action action;
    private final Reachable parent;
    public ActionProxiesImpl(Action action, Reachable parent) {
        this.action = action;
        this.parent = parent;
    }


    @Override
    public Object getAction() {
        if(action.getClass().isAnnotationPresent(ExportedBean.class)){
            return action;
        }else{
            return null;
        }
    }

    @Override
    public String getUrlName() {
        return action.getUrlName();
    }

    @Override
    public String get_Class() {
        return action.getClass().getName();
    }

    @Override
    public Link getLink() {
        if(getUrlName() != null) {
            return parent.getLink().rel(getUrlName());
        }
        return null;
    }
}
