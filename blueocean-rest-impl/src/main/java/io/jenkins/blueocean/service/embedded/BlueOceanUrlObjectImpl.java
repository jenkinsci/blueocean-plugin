package io.jenkins.blueocean.service.embedded;

import hudson.model.ModelObject;
import io.jenkins.blueocean.rest.factory.BlueOceanUrlMapper;
import io.jenkins.blueocean.rest.model.BlueOceanUrlObject;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
public class BlueOceanUrlObjectImpl extends BlueOceanUrlObject {

    private final String mappedUrl;

    public BlueOceanUrlObjectImpl(ModelObject modelObject) {
        String url = null;
        for(BlueOceanUrlMapper mapper: BlueOceanUrlMapper.all()){
            url = mapper.getUrl(modelObject);
            if(url != null){
                break;
            }
        }
        if(url == null){
            url = BlueOceanUrlMapperImpl.getLandingPagePath();
        }
        this.mappedUrl = url;
    }

    @Override
    public @Nonnull String getDisplayName() {
        return Messages.BlueOceanUrlAction_DisplayName();
    }

    @Override
    public @Nonnull String getUrl() {
        return mappedUrl;
    }

    @Override
    public @Nonnull String getIconUrl() {
        return "/plugin/blueocean-rest-impl/images/48x48/blueocean.png";
    }
}
