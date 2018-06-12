package io.jenkins.blueocean.rest.factory;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
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
     * @param organization that the parent is a child of
     * @return null if it doesn't knows about this instance of run object, otherwise instance of BlueRun
     */
    public abstract BlueRun getRun(Run run, Reachable parent, BlueOrganization organization);

    /**
     * @param r run
     * @param parent of run
     * @return run or null
     */
    public static BlueRun getRun(Run r, Reachable parent){
        BlueOrganization organization = OrganizationFactory.getInstance().getContainingOrg(r.getParent());
        if (organization == null) {
            return null;
        }
        for(BlueRunFactory runFactory:ExtensionList.lookup(BlueRunFactory.class)){
            BlueRun blueRun = runFactory.getRun(r,parent, organization);
            if(blueRun != null){
                return blueRun;
            }
        }
        return null;
    }
}
