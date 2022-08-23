package io.jenkins.blueocean.service.embedded;

import hudson.Extension;
import hudson.model.ModelObject;
import io.jenkins.blueocean.rest.factory.BlueOceanUrlObjectFactory;
import io.jenkins.blueocean.rest.model.BlueOceanUrlObject;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal = -9999)
public class BlueOceanUrlObjectFactoryImpl extends BlueOceanUrlObjectFactory {
    @NonNull
    @Override
    public BlueOceanUrlObject get(@NonNull final ModelObject object) {
        return new BlueOceanUrlObjectImpl(object);
    }
}
