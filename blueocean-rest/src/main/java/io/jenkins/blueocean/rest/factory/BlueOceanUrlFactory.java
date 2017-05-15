package io.jenkins.blueocean.rest.factory;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.ModelObject;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * {@link hudson.ExtensionPoint} to provide BlueOcean URL for Jenkins model object
 *
 * @author Vivek Pandey
 * @see BlueOceanUrlAction
 */
public abstract class BlueOceanUrlFactory implements ExtensionPoint{
    /**
     * Gives {@link BlueOceanUrlAction} for given {@link ModelObject}.
     *
     * @param object Jenkins {@link ModelObject}
     * @return BlueOceanAction
     */
    public abstract @Nonnull BlueOceanUrlAction get(@Nonnull ModelObject object);

    public static ExtensionList<BlueOceanUrlFactory> all(){
        return ExtensionList.lookup(BlueOceanUrlFactory.class);
    }

    public @CheckForNull static BlueOceanUrlFactory getFirst(){
        for(BlueOceanUrlFactory f: all()){
            return f;
        }
        return null;
    }
}
