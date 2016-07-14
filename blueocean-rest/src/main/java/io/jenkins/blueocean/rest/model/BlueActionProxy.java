package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;

/**
 * Proxy of Jenkins action
 *
 * @author Vivek Pandey
 */
public abstract class BlueActionProxy extends Resource {

    /**
     * Proxied action instance
     *
     * @return action instance
     */
    @Exported(name="action", merge = true)
    public abstract Object getAction();

    /**
     * urlName is the HTTP URL path name that extends base URL path
     *
     * For example: Pipeline RUN Test Result action has urlName value as 'testResult'
     *
     * It can be accessed at URL path: .../pipelines/p1/runs/2/testResult
     *
     * @return urlName
     */
    @Exported(name = "urlName")
    public abstract String getUrlName();

    /**
     * _class is the class name of action.
     */
    @Exported(name = "_class")
    public abstract String get_Class();
}
