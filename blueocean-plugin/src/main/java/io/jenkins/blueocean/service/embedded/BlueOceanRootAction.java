package io.jenkins.blueocean.service.embedded;

import hudson.Extension;
import hudson.model.RootAction;
import io.jenkins.blueocean.BlueOceanUI;
import org.kohsuke.stapler.StaplerProxy;

import javax.inject.Inject;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
public class BlueOceanRootAction implements RootAction, StaplerProxy {
    @Inject
    BlueOceanUI app;

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    /**
     * This would map to /jenkins/blue/
     */
    @Override
    public String getUrlName() {
        return app.getUIUrlBase();
    }

    @Override
    public Object getTarget() {
        return app;
    }
}
