package io.jenkins.blueocean;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.ModelObject;
import io.jenkins.blueocean.rest.factory.BlueOceanUrlAction;
import io.jenkins.blueocean.rest.factory.BlueOceanUrlFactory;
import jenkins.model.TransientActionFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal = -9999)
public class BlueOceanUrlFactoryImpl extends BlueOceanUrlFactory{

    @Nonnull
    @Override
    public BlueOceanUrlAction get(final ModelObject object) {
        return new BlueOceanUrlActionImpl(object);
    }

    @Extension
    public static class TransientActionFactoryImpl extends TransientActionFactory<ModelObject> {
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
            BlueOceanUrlFactory f = getFirst();
            if(f != null){
                return Collections.singleton(f.get(target));
            }
            return Collections.emptyList();
        }
    }
}
