package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_FAVORITE;

/**
 * A favorite item. The item itself must be JSON serializable bean
 *
 * @author Ivan Meredith
 * @author Vivek Pandey
 */
@ExportedBean
@Capability(BLUE_FAVORITE)
public abstract class BlueFavorite extends Resource{
    private static final String ITEM = "item";


    /**
     * @return Gives favorite item
     */
    @Exported(name = ITEM, inline = true)
    public  abstract Object getItem();
}
