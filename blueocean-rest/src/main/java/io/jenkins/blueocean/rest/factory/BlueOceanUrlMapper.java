package io.jenkins.blueocean.rest.factory;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.ModelObject;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Maps Jenkins {@link ModelObject} to BlueOcean front end URL
 *
 * @author Vivek Pandey
 */
public abstract class BlueOceanUrlMapper implements ExtensionPoint{
    /**
     * Gives BlueOcean URL for given Jenkins {@link ModelObject}.
     *
     * @param modelObject Jenkins ModelObject
     * @return Gives url for this model object, returns null if it can't compute URL for this model object
     */
    @CheckForNull
    public abstract String getUrl(@NonNull ModelObject modelObject);

    public static ExtensionList<BlueOceanUrlMapper> all(){
        return ExtensionList.lookup(BlueOceanUrlMapper.class);
    }
}
