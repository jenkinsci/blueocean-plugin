package io.jenkins.blueocean.analyticstools;

import hudson.Extension;
import io.jenkins.blueocean.BluePageDecorator;
import jenkins.model.Jenkins;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal = 10)
public class AnalyticsTools extends BluePageDecorator {

    public boolean isRollBarEnabled(){
        return Boolean.getBoolean("BLUEOCEAN_ROLLBAR_ENABLED");
    }


    /** gives Blueocean plugin version. blueocean-web being core module is looked at to determine the version */
    public String getBlueOceanPluginVersion(){
        return Jenkins.getInstance().getPlugin("blueocean-web").getWrapper().getVersion();
    }
}
