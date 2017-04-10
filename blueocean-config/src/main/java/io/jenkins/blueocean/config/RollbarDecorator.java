package io.jenkins.blueocean.config;

import hudson.Extension;
import io.jenkins.blueocean.BluePageDecorator;
import io.jenkins.blueocean.commons.BlueOceanConfigProperties;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal = 10)
public class RollbarDecorator extends BluePageDecorator {

    public boolean isRollBarEnabled(){
        return BlueOceanConfigProperties.ROLLBAR_ENABLED;
    }
}
