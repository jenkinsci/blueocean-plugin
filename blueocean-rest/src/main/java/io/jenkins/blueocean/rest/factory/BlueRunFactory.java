package io.jenkins.blueocean.rest.factory;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.model.BlueRun;

/**
 * Factory that gives instance of {@link BlueRun}
 *
 * @author Vivek Pandey
 */
public abstract class BlueRunFactory implements ExtensionPoint {

    /**
     * Gives instance of {@link BlueRun} that this factory knows about
     * @param run Jenkins run model object
     * @param parent {@link Reachable} parent. This is to be used to create this BlueRun instance's Link.
     * @return null if it doesn't knows about this instance of run object, otherwise instance of BlueRun
     */
    public abstract BlueRun getRun(Run run, Reachable parent);

    public static ExtensionList<BlueRunFactory> all(){
        return ExtensionList.lookup(BlueRunFactory.class);
    }
}
