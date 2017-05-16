package io.jenkins.blueocean;

/**
 * @author Vivek Pandey
 */

import hudson.Extension;
import hudson.model.Action;
import hudson.model.ModelObject;
import io.jenkins.blueocean.rest.factory.BlueOceanUrlActionFactory;
import jenkins.model.TransientActionFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

import static io.jenkins.blueocean.rest.factory.BlueOceanUrlActionFactory.getFirst;

/**
 * Adds 'Open Blue Ocean' menu on the left side of Jenkins pages.
 *
 * @see BlueOceanUrlActionFactory
 * @see io.jenkins.blueocean.rest.factory.BlueOceanUrlAction
 * @see io.jenkins.blueocean.rest.factory.BlueOceanUrlMapper
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
        BlueOceanUrlActionFactory f = getFirst();
        if(f != null){
            return Collections.singleton(f.get(target));
        }
        return Collections.emptyList();
    }
}
