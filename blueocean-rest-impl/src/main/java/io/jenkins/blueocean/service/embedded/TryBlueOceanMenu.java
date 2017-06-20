package io.jenkins.blueocean.service.embedded;

/**
 * @author Vivek Pandey
 */

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.ModelObject;
import io.jenkins.blueocean.rest.factory.BlueOceanUrlObjectFactory;
import io.jenkins.blueocean.rest.model.BlueOceanUrlObject;
import jenkins.model.TransientActionFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

import static io.jenkins.blueocean.rest.factory.BlueOceanUrlObjectFactory.getFirst;

/**
 * Adds 'Open Blue Ocean' menu on the left side of Jenkins pages.
 *
 * @see BlueOceanUrlObjectFactory
 */
@Extension
public class TryBlueOceanMenu extends TransientActionFactory<ModelObject> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<ModelObject> type() {
        return ModelObject.class;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Collection<? extends Action> createFor(@Nonnull ModelObject target) {
        // we do not report actions as it might appear multiple times, we simply add it to Actionable
        BlueOceanUrlObjectFactory f = getFirst();
        if(f != null){
            BlueOceanUrlObject blueOceanUrlObject = f.get(target);
            BlueOceanUrlAction a = new BlueOceanUrlAction(blueOceanUrlObject);
            if(target instanceof Actionable){
                if(exists((Actionable) target, a)){
                    return Collections.emptyList();
                }
                try {
                    ((Actionable) target).replaceAction(a);
                }catch (UnsupportedOperationException e){
                    //ignore, replace is not supported
                }
                return Collections.singleton(a);
            }
        }
        return Collections.emptyList();
    }

    private boolean exists(Actionable actionable, BlueOceanUrlAction blueOceanUrlAction){
        for(Action a: actionable.getActions()) {
            // JENKINS-44926 only call getURLName on these actions if action is BLueOceanUrlAction
            if (a instanceof BlueOceanUrlAction) {
                String blueUrl  = blueOceanUrlAction.getUrlName();
                String thisUrl = a.getUrlName();
                if(thisUrl != null && thisUrl.equals(blueUrl)){
                    return true;
                }
            }
        }
        return false;
    }
}
