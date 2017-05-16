package io.jenkins.blueocean.rest.factory;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.ModelObject;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * {@link hudson.ExtensionPoint} to provide {@link BlueOceanUrlAction} for Jenkins model object
 *
 * @author Vivek Pandey
 * @see BlueOceanUrlAction
 */
public abstract class BlueOceanUrlActionFactory implements ExtensionPoint{
    /**
     * Gives {@link BlueOceanUrlAction} for given {@link ModelObject}.
     *
     * @param object Jenkins {@link ModelObject}
     * @return BlueOceanAction
     */
    public abstract @Nonnull BlueOceanUrlAction get(@Nonnull ModelObject object);

    public static ExtensionList<BlueOceanUrlActionFactory> all(){
        return ExtensionList.lookup(BlueOceanUrlActionFactory.class);
    }

    public @CheckForNull static BlueOceanUrlActionFactory getFirst(){
        for(BlueOceanUrlActionFactory f: all()){
            return f;
        }
        return null;
    }
}
