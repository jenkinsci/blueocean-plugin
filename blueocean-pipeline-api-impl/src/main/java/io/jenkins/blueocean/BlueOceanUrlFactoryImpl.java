package io.jenkins.blueocean;

import hudson.Extension;
import hudson.model.ModelObject;
import io.jenkins.blueocean.rest.factory.BlueOceanUrlAction;
import io.jenkins.blueocean.rest.factory.BlueOceanUrlFactory;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal = -9999)
public class BlueOceanUrlFactoryImpl extends BlueOceanUrlFactory{
    public BlueOceanUrlFactoryImpl() {
        Jenkins.getInstance().getActions().add(new BlueOceanUrlActionImpl());
    }

    @Nonnull
    @Override
    public BlueOceanUrlAction get(@Nonnull final ModelObject object) {
        return new BlueOceanUrlActionImpl(object);
    }

}
