package io.jenkins.blueocean.rest.factory;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.ModelObject;
import io.jenkins.blueocean.rest.model.BlueOceanUrlObject;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * {@link hudson.ExtensionPoint} to provide {@link BlueOceanUrlObject} for Jenkins model object
 *
 * @author Vivek Pandey
 */
public abstract class BlueOceanUrlObjectFactory implements ExtensionPoint{
    /**
     * Gives {@link BlueOceanUrlObject} for given {@link ModelObject}.
     *
     * @param object Jenkins {@link ModelObject}
     * @return BlueOceanUrlObject
     */

    public abstract @Nonnull BlueOceanUrlObject get(@Nonnull ModelObject object);

    public static ExtensionList<BlueOceanUrlObjectFactory> all(){
        return ExtensionList.lookup(BlueOceanUrlObjectFactory.class);
    }

    public @CheckForNull static BlueOceanUrlObjectFactory getFirst(){
        for(BlueOceanUrlObjectFactory f: all()){
            return f;
        }
        return null;
    }
}
