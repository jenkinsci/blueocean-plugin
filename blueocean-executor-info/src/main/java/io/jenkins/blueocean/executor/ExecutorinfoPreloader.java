package io.jenkins.blueocean.executor;

import hudson.Extension;
import io.jenkins.blueocean.commons.PageStatePreloader;

/**
 * Provides configuration data, basic usage:
 * import { blueocean } from '@jenkins-cd/blueocean-core-js/dist/js/scopes';
 * data available at: blueocean.executor.showInfo
 */
@Extension
public class ExecutorinfoPreloader extends PageStatePreloader {
    static boolean showExecutorInfo = !Boolean.getBoolean("blueocean-executor-info-plugin.disable-executor-info");

    @Override
    public String getStatePropertyPath() {
        return "executor";
    }

    @Override
    public String getStateJson() {
        return "{showInfo:" + showExecutorInfo + "}";
    }
}
