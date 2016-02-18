package io.jenkins.embryo;

import hudson.Extension;
import io.jenkins.blueocean.BlueOceanUI;
import org.kohsuke.stapler.StaplerProxy;

import javax.inject.Inject;

/**
 * Glue to map {@link BlueOceanUI} at the root of the URL space.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class BlueOceanApp extends App implements StaplerProxy {
    @Inject
    BlueOceanUI app;

    @Override
    public Object getTarget() {
        return app;
    }
}
