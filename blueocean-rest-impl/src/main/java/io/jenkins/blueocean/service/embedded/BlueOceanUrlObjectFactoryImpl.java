package io.jenkins.blueocean.service.embedded;

import hudson.Extension;
import hudson.model.ModelObject;
import io.jenkins.blueocean.rest.factory.BlueOceanUrlObjectFactory;
import io.jenkins.blueocean.rest.model.BlueOceanUrlObject;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal = -9999)
public class BlueOceanUrlObjectFactoryImpl extends BlueOceanUrlObjectFactory {
    @Nonnull
    @Override
    public BlueOceanUrlObject get(@Nonnull final ModelObject object) {
        return new BlueOceanUrlObjectImpl(object);
    }
}
