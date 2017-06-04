package io.jenkins.blueocean.service.embedded;

import com.google.common.base.Preconditions;
import hudson.model.Action;
import io.jenkins.blueocean.rest.model.BlueOceanUrlObject;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
public final class BlueOceanUrlAction implements Action {
    private final BlueOceanUrlObject blueOceanUrlObject;

    public BlueOceanUrlAction(@Nonnull BlueOceanUrlObject urlObject) {
        Preconditions.checkNotNull(urlObject);
        this.blueOceanUrlObject = urlObject;
    }

    @Override
    public String getIconFileName() {
        return blueOceanUrlObject.getIconUrl();
    }

    @Override
    public String getDisplayName() {
        return blueOceanUrlObject.getDisplayName();
    }

    @Override
    public String getUrlName() {
        return blueOceanUrlObject.getUrl();
    }

    BlueOceanUrlObject getBlueOceanUrlObject(){
        return blueOceanUrlObject;
    }
}
