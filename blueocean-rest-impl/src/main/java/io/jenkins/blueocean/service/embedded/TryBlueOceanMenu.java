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

import edu.umd.cs.findbugs.annotations.NonNull;
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
    @NonNull
    @Override
    public Collection<? extends Action> createFor(@NonNull ModelObject target) {
        // we do not report actions as it might appear multiple times, we simply add it to Actionable
        BlueOceanUrlObjectFactory f = getFirst();
        if(f != null) {
            // TODO remove this if block once we are using a core > 2.126
            // Work around JENKINS-51584
            if (target instanceof hudson.model.Queue.Item) {
                return Collections.emptyList();
            }
            BlueOceanUrlObject blueOceanUrlObject = f.get(target);
            BlueOceanUrlAction a = new BlueOceanUrlAction(blueOceanUrlObject);
            return Collections.singleton(a);
        }
        return Collections.emptyList();
    }

    @Override
    public Class<? extends Action> actionType() {
        return BlueOceanUrlAction.class;
    }
}
